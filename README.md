# Hydrophobia
A configurable Minecraft plugin for damaging entities in water, built with Kotlin using the Spigot API.

## Usage

1. Clone the repository: `git clone https://github.com/groovin/hydrophobia.git`
2. Build the project:
   ```bash
   ./gradlew build
   ```
   This will generate two JAR files in `build/libs/`:
   - `hydrophobia-<version>.jar` - Standard plugin jar
   - `hydrophobia-<version>-offline.jar` - Shadowed jar with all dependencies included

3. Place either JAR file in your server's `plugins` directory
4. Configure the plugin through the generated `config.yml` file
5. Restart your server or reload the plugin

## Commands

- `/hydrophobia` - Main command for Hydrophobia plugin

## Permissions

- `hydrophobia.admin` - Allows access to all Hydrophobia commands (default: op)
- `hydrophobia.bypass` - Allows bypassing water damage (default: op)

### Building from Source

1. Ensure you have JDK 21 installed
2. Clone the repository
3. Run `./gradlew build`

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
