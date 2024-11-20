# status-bar
This is the status bar application for lcarsde. It is used to display general system information, provide basic system controls and look awesome.

## How-to for widgets
The status bar is an application that consists of a freely configurable layout. It uses a configuration file to set up the widgets correctly in the application. The application offers a grid with 40px by 40px cells with 8px gaps. The grid is always 3 cells high. The width depends on the screen resolution. There might be space left, that is not possible to put in the 40x40 grid. That will be filled with special place holder widgets.

A widget may support different cell-based sizes. The final width and height can be set in the configuration file. The configuration file will contain the position and size for each widget in the grid. If a widget stretches over multiple cells it will also stretch over the 8px gaps.

*Example*: A widget supports a width of 3 cells and a height of 1 cell. Its width will be 40px + 8px + 40px + 8px + 40px = 136px wide and 40px high. The configuration needs to state this mode as width = 3 and height = 1.

## Currently available widgets
* *LcarsdeStatusTime* - Shows the current time
* *LcarsdeStatusDate* - Shows the current date
* *LcarsdeStatusStardate* - Shows the current star date (based on some TNG time calculation I found somewhere)
* *LcarsdeStatusTemperature* - Shows a graph with the temperatures from the computer temperature sensors
* *LcarsdeStatusCpuUsage* - Shows a graph with the current usages of the different CPU cores
* *LcarsdeStatusAudio* - Shows the volume level and provides buttons for basic audio actions; The audio status is provided by some executable that prints `volume;muted` where volume is a number from 0 to 100 and muted is yes or 1 for muted and anything else for unmuted
* *LcarsdeBatteryStatus* - Shows the computers battery status
* *LcarsdeWifiStatus* - Shows the status of a wifi connector
* *LcarsdeEthStatus* - Shows the status of an ethernet connector
* *LcarsdeStatusButton* - Button for executing a command/running a program
* *LcarsdeStatusMemory* - Shows the current memory usage
* *LcarsdeStatusFiller* - Used to fill empty space in the status bar; there's usually no need to explicitly configure it
