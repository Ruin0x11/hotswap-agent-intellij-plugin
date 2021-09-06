HotSwapAgent IntelliJ IDEA Plugin - JDK 11 + DCEVM
=================================
This is [hotswap-agent-intellij-plugin](https://github.com/dmitry-zhuravlev/hotswap-agent-intellij-plugin) updated to use [trava-jdk-11-dcevm](https://github.com/TravaOpenJDK/trava-jdk-11-dcevm).

## Installation

1. Download the latest DCEVM 11 from [here](https://github.com/TravaOpenJDK/trava-jdk-11-dcevm/releases) and extract it somewhere.
2. Install the plugin in IntelliJ.
3. Add the DCEVM folder to your project's SDKs and configure your project to use it.
4. Enable HotSwapAgent under `File > Settings > Tools > HotSwapAgent`.
5. Configure IntelliJ's class reloading under `File > Settings > Build, Execution, Deployment > Debugger > HotSwap`.

## Usage

1. Start your project in **Debug** mode.
2. Make some changes to a class and run `Build > Recompile File`.

## Credits

The original code was written by Dmitry Zhuravlev and Sergei Stepanov.

## License

As parts of the DCEVM codebase are used, this project was relicensed from Apache 2.0 to GPLv3.