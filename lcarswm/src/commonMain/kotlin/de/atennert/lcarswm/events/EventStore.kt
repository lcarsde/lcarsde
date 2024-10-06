package de.atennert.lcarswm.events

import de.atennert.lcarswm.keys.WmAction
import de.atennert.rx.Subject

internal val keyPressSj = Subject<WmAction>()
val keyPressObs = keyPressSj.asObservable()
