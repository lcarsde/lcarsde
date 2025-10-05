package de.atennert.lcarswm.lifecycle

import de.atennert.lcarsde.lifecycle.closeWith
import de.atennert.lcarswm.*
import de.atennert.lcarswm.atom.AtomLibrary
import de.atennert.lcarswm.atom.NumberAtomReader
import de.atennert.lcarswm.atom.TextAtomReader
import de.atennert.lcarswm.command.Commander
import de.atennert.lcarswm.command.PosixCommander
import de.atennert.lcarswm.drawing.ColorFactory
import de.atennert.lcarswm.drawing.FontProvider
import de.atennert.lcarswm.drawing.FrameDrawer
import de.atennert.lcarswm.drawing.RootWindowDrawer
import de.atennert.lcarswm.environment.Environment
import de.atennert.lcarswm.events.*
import de.atennert.lcarswm.file.Files
import de.atennert.lcarswm.keys.FocusSessionKeyboardGrabber
import de.atennert.lcarswm.keys.KeyConfiguration
import de.atennert.lcarswm.keys.KeyManager
import de.atennert.lcarswm.keys.KeySessionManager
import de.atennert.lcarswm.log.Logger
import de.atennert.lcarswm.monitor.MonitorManager
import de.atennert.lcarswm.monitor.MonitorManagerImpl
import de.atennert.lcarswm.mouse.MoveWindowManager
import de.atennert.lcarswm.settings.SettingsReader
import de.atennert.lcarswm.signal.Signal
import de.atennert.lcarswm.signal.SignalHandler
import de.atennert.lcarswm.system.MessageQueue
import de.atennert.lcarswm.system.api.SystemApi
import de.atennert.lcarswm.window.*
import de.atennert.rx.NextObserver
import de.atennert.rx.operators.map
import de.atennert.rx.operators.withLatestFrom
import de.atennert.rx.util.Tuple
import exitState
import kotlinx.atomicfu.AtomicRef
import kotlinx.cinterop.*
import platform.posix.waitpid
import staticLogger
import xlib.*
import kotlin.experimental.ExperimentalNativeApi

private var wmDetected = false

@ExperimentalForeignApi
const val ROOT_WINDOW_MASK = SubstructureRedirectMask or StructureNotifyMask or PropertyChangeMask or
        FocusChangeMask or KeyPressMask or KeyReleaseMask

@ExperimentalForeignApi
private const val XRANDR_MASK = RRScreenChangeNotifyMask or RROutputChangeNotifyMask or
        RRCrtcChangeNotifyMask or RROutputPropertyNotifyMask

@ExperimentalForeignApi
@ExperimentalNativeApi
fun startup(system: SystemApi, logger: Logger, resourceGenerator: ResourceGenerator): RuntimeResources? {
    val eventStore = EventStore()
    val commander = PosixCommander(logger)
    val files = resourceGenerator.createFiles()
    val environment = resourceGenerator.createEnvironment()
    val fileFactory = resourceGenerator.createFileFactory()
    val signalHandler = SignalHandler(system)

    wmDetected = false
    if (!system.openDisplay()) {
        logger.logError("::startup::got no display")
        return null
    }
    system.closeWith { closeDisplay() }

    val randrHandlerFactory = RandrHandlerFactory(system, logger)

    val atomLibrary = AtomLibrary(system)

    val keyManager = KeyManager(system)

    val eventBuffer = EventBuffer(system)

    listOf(
        Signal.USR1,
        Signal.USR2,
        Signal.TERM,
        Signal.INT,
        Signal.HUP,
        Signal.PIPE,
        Signal.CHLD,
        Signal.TTIN,
        Signal.TTOU
    )
        .forEach {
            signalHandler.addSignalCallback(it, staticCFunction { signal ->
                handleSignal(
                    signal,
                    exitState
                )
            })
        }

    val screen = system.defaultScreenOfDisplay()?.pointed
    if (screen == null) {
        logger.logError("::startup::got no screen")
        return null
    }

    system.synchronize(false)

    setDisplayEnvironment(system, environment)

    val rootWindowPropertyHandler = RootWindowPropertyHandler(
        logger,
        system,
        screen.root,
        atomLibrary,
        eventBuffer
    )

    val eventTime = EventTime(system, eventBuffer, atomLibrary, rootWindowPropertyHandler)

    if (!rootWindowPropertyHandler.becomeScreenOwner(eventTime)) {
        logger.logError("::startup::Detected another active window manager")
        return null
    }

    system.sync(false)
    system.setErrorHandler(staticCFunction { _, _ -> wmDetected = true; 0 })

    system.selectInput(screen.root, ROOT_WINDOW_MASK)

    system.sync(false)

    if (wmDetected) {
        logger.logError("::startup::Detected another active window manager")
        return null
    }
    system.closeWith { selectInput(screen.root, NoEventMask) }

    system.setErrorHandler(staticCFunction { _, err -> staticLogger?.logError("::startup::error code: ${err?.pointed?.error_code}"); 0 })

    system.defineCursor(screen.root, system.createFontCursor(XC_left_ptr))

    rootWindowPropertyHandler
        .closeWith(RootWindowPropertyHandler::unsetWindowProperties)
        .setSupportWindowProperties()

    eventTime.resetEventTime()

    val settings = loadSettings(logger, system, files, environment)
    if (settings == null) {
        logger.logError("::startup::unable to load settings")
        return null
    }

    logger.logDebug("::startup::Screen size: ${screen.width}/${screen.height}, root: ${screen.root}")

    val monitorManager = MonitorManagerImpl(system, screen.root)

    val fontProvider = FontProvider(system, settings.generalSettings, system.defaultScreenNumber())
    val colorHandler = ColorFactory(system, screen)
    val uiDrawer =
        RootWindowDrawer(system, system, monitorManager, screen, colorHandler, settings.generalSettings, fontProvider)

    keyManager.ungrabAllKeys(screen.root)

    val env = de.atennert.lcarswm.Environment(system.display)

    val toggleSessionManager = KeySessionManager(logger, env)

    val keyConfiguration = KeyConfiguration(
        system,
        settings.keyBindings,
        keyManager,
        toggleSessionManager,
        screen.root
    )

    system.sync(false)

    val windowList = WindowList()

    val appMenuMessageQueue = MessageQueue("/lcarswm-app-menu-messages", MessageQueue.Mode.READ, true)

    val appMenuMessageHandler = AppMenuMessageHandler(
        logger,
        system,
        atomLibrary,
        windowList,
    )

    val focusHandler = WindowFocusHandler(windowList, appMenuMessageHandler)

    WindowStack(system.display, windowList, focusHandler)

    val frameDrawer = FrameDrawer(system, system, fontProvider, colorHandler, screen)

    val textAtomReader = TextAtomReader(system, atomLibrary)
    val numberAtomReader = NumberAtomReader(system.display, atomLibrary)

    val windowListMessageQueue = MessageQueue("/lcarswm-active-window-list", MessageQueue.Mode.WRITE, true)

    val windowFactory = PosixWindowFactory(
        logger,
        system.display,
        screen,
        colorHandler,
        fontProvider,
        keyManager,
        monitorManager,
        atomLibrary,
        eventStore,
        focusHandler,
        windowList,
        textAtomReader,
        numberAtomReader,
        frameDrawer,
        windowListMessageQueue
    )

    val windowCoordinator =
        PosixWindowCoordinator(
            logger,
            eventStore,
            monitorManager,
            windowFactory,
            windowList,
            uiDrawer,
            screen.display
        )

    val modeButton = windowFactory.createButton(
        "MODE",
        COLOR_4,
        0,
        0,
        SIDE_BAR_WIDTH,
        BAR_HEIGHT
    ) {
        monitorManager.toggleFramedScreenMode()
    }

    val screenChangeHandler =
        setupRandr(
            system,
            randrHandlerFactory,
            monitorManager,
            screen.root
        )

    val moveWindowManager = MoveWindowManager(logger, windowCoordinator, monitorManager)

    focusHandler.registerObserver(
        FocusSessionKeyboardGrabber(system, eventTime, rootWindowPropertyHandler.ewmhSupportWindow)
    )

    focusHandler.registerObserver(InputFocusHandler(logger, system, eventTime, screen.root))

    focusHandler.windowFocusEventObs
        .apply(withLatestFrom(windowList.windowsObs))
        .apply(map { (event, windows) ->
            Tuple(
                windows.find { it.id == event.oldWindow },
                windows.find { it.id == event.newWindow }
            )
        })
        .subscribe(NextObserver { (oldWindow, newWindow) ->
            oldWindow?.unfocus()
            newWindow?.focus()
        })

    updateWindowListAtom(screen.root, system, atomLibrary, windowList)

    toggleSessionManager.addListener(focusHandler.keySessionListener)

    val eventManager = createEventManager(
        eventStore,
        system,
        logger,
        monitorManager,
        windowCoordinator,
        focusHandler,
        keyManager,
        moveWindowManager,
        toggleSessionManager,
        atomLibrary,
        screenChangeHandler,
        keyConfiguration,
        windowList,
        commander,
        screen.root,
        modeButton
    )

    setupScreen(
        system,
        screen.root,
        rootWindowPropertyHandler,
        windowFactory
    )

    val xEventResources = XEventResources(eventManager, eventTime, eventBuffer)
    val appMenuResources = AppMenuResources(appMenuMessageHandler, appMenuMessageQueue)
    val platformResources = PlatformResources(commander, fileFactory, files, monitorManager, environment)

    return RuntimeResources(xEventResources, appMenuResources, platformResources)
}

/**
 * Signal handler for usual stuff.
 */
@ExperimentalForeignApi
private fun handleSignal(signalValue: Int, exitState: AtomicRef<Int?>) {
    val signal = Signal.values().single { it.signalValue == signalValue }
    staticLogger?.logDebug("::handleSignal::signal: $signal")
    when (signal) {
        Signal.USR1 -> staticLogger?.logInfo("Ignoring signal $signal")
        Signal.USR2 -> staticLogger?.logInfo("Ignoring signal $signal")
        Signal.TTIN -> staticLogger?.logInfo("Ignoring signal $signal")
        Signal.TTOU -> staticLogger?.logInfo("Ignoring signal $signal")
        Signal.CHLD -> while (waitpid(-1, null, platform.posix.WNOHANG) > 0);
        Signal.TERM -> exitState.value = 0
        Signal.INT -> exitState.value = 0
        else -> exitState.value = 1
    }
}

@ExperimentalForeignApi
private fun setDisplayEnvironment(system: SystemApi, environment: Environment) {
    val displayString = system.getDisplayString()
    environment["DISPLAY"] = displayString
}

@ExperimentalForeignApi
private fun setupScreen(
    system: SystemApi,
    rootWindow: Window,
    rootWindowPropertyHandler: RootWindowPropertyHandler,
    windowFactory: WindowFactory<Window>
) {
    system.grabServer()

    val returnedWindows = ULongArray(1)
    val returnedParent = ULongArray(1)
    val topLevelWindows = nativeHeap.allocPointerTo<ULongVarOf<Window>>()
    val topLevelWindowCount = UIntArray(1)

    system.queryTree(
        rootWindow, returnedWindows.toCValues(), returnedParent.toCValues(),
        topLevelWindows.ptr,
        topLevelWindowCount.toCValues()
    )

    ULongArray(topLevelWindowCount[0].convert()) { topLevelWindows.value!![it] }
        .filter { childId -> childId != rootWindow }
        .filter { childId -> childId != rootWindowPropertyHandler.ewmhSupportWindow }
        .forEach { childId ->
            windowFactory.createWindow(childId, true)
        }

    nativeHeap.free(topLevelWindows)
    system.ungrabServer()
}

/**
 * Load the key configuration from the users key configuration file.
 */
@ExperimentalForeignApi
private fun loadSettings(
    logger: Logger,
    systemApi: SystemApi,
    files: Files,
    environment: Environment
): SettingsReader? {
    val configPath = environment[HOME_CONFIG_DIR_PROPERTY] ?: return null

    return SettingsReader(logger, systemApi, files, configPath)
}

/**
 * @return RANDR base value
 */
@ExperimentalForeignApi
private fun setupRandr(
    system: SystemApi,
    randrHandlerFactory: RandrHandlerFactory,
    monitorManager: MonitorManager<*>,
    rootWindowId: Window
): XEventHandler {
    val screenChangeHandler = randrHandlerFactory.createScreenChangeHandler(monitorManager)
    val fakeEvent = nativeHeap.alloc<XEvent>()
    screenChangeHandler.handleEvent(fakeEvent)
    nativeHeap.free(fakeEvent)

    system.rSelectInput(rootWindowId, XRANDR_MASK.convert())

    return screenChangeHandler
}

/**
 * Create the event handling for the event loop.
 */
@ExperimentalForeignApi
private fun createEventManager(
    eventStore: EventStore,
    system: SystemApi,
    logger: Logger,
    monitorManager: MonitorManager<*>,
    windowCoordinator: WindowCoordinator,
    focusHandler: WindowFocusHandler,
    keyManager: KeyManager,
    moveWindowManager: MoveWindowManager,
    toggleSessionManager: KeySessionManager,
    atomLibrary: AtomLibrary,
    screenChangeHandler: XEventHandler,
    keyConfiguration: KeyConfiguration,
    windowList: WindowList,
    commander: Commander,
    rootWindowId: Window,
    modeButton: Button<Window>,
): EventDistributor {

    return EventDistributor.Builder(logger)
        .addEventHandler(ConfigureRequestHandler(logger, eventStore))
        .addEventHandler(DestroyNotifyHandler(logger, eventStore))
        .addEventHandler(ButtonPressHandler(logger, system, windowList, focusHandler, moveWindowManager, modeButton))
        .addEventHandler(ButtonReleaseHandler(logger, moveWindowManager, modeButton))
        .addEventHandler(
            KeyPressHandler(
                logger,
                keyManager,
                keyConfiguration,
                toggleSessionManager,
                monitorManager,
                windowCoordinator,
                focusHandler
            )
        )
        .addEventHandler(
            KeyReleaseHandler(
                logger,
                system,
                focusHandler,
                keyManager,
                keyConfiguration,
                toggleSessionManager,
                atomLibrary,
                commander
            )
        )
        .addEventHandler(MapRequestHandler(logger, eventStore))
        .addEventHandler(MotionNotifyHandler(logger, moveWindowManager))
        .addEventHandler(UnmapNotifyHandler(logger, eventStore))
        .addEventHandler(screenChangeHandler)
        .addEventHandler(ReparentNotifyHandler(logger, eventStore))
        .addEventHandler(ClientMessageHandler(logger, atomLibrary))
        .addEventHandler(SelectionClearHandler(logger))
        .addEventHandler(MappingNotifyHandler(logger, keyManager, keyConfiguration, rootWindowId))
        .addEventHandler(PropertyNotifyHandler(atomLibrary, eventStore))
        .addEventHandler(EnterNotifyHandler(logger, eventStore))
        .addEventHandler(LeaveNotifyHandler(logger, eventStore))
        .build()
}
