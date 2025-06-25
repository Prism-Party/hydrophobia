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

---

## GitHub Actions CI/CD

This repository uses robust GitHub Actions workflows for CI, releases, and snapshot builds. **No Docker or custom release actions are required.**

### Workflows

- **ci.yml**: Runs on every push and pull request to `main`/`master`. It tests, builds, and uploads artifacts. On push, it also creates a GitHub Release with the built JARs.
- **release.yml**: Manual workflow for maintainers to create a new version/tagged release. Trigger it from the GitHub Actions UI, select the version increment (patch, minor, major, prerelease), and it will:
  - Run the axion-release plugin to bump the version and tag
  - Build the project
  - Create a GitHub Release with the new version and upload JARs
- **snapshot.yml**: Runs on push to `develop`/`dev` or manually. Builds the project and creates a pre-release (snapshot) on GitHub for testing.

### How to Use

- **CI (default):**
  - On PRs: Runs tests and build, uploads artifacts for inspection.
  - On push to `main`/`master`: Also creates a GitHub Release with the JARs.

- **Manual Release:**
  - Go to the Actions tab, select "Manual Release", and click "Run workflow".
  - Choose the version increment type (default, patch, minor, major, prerelease).
  - The workflow will tag, build, and release the new version.

- **Snapshot Builds:**
  - On push to `develop`/`dev` or by running the workflow manually, a snapshot pre-release is created for testing.

### Configuration

- **Versioning:** Uses [axion-release-plugin](https://axion-release-plugin.readthedocs.io/) for semantic versioning.
- **Release Artifacts:** All JARs in `build/libs/` are attached to releases.
- **No Docker Required:** All workflows use standard GitHub Actions and Gradle plugins.

### Secrets

- `GITHUB_TOKEN` is used for releases and is provided by GitHub Actions by default.
- For manual release/tagging, you may need to grant write permissions to the workflow in repository settings.

---

**For template users:**
- Copy the `.github/workflows/` files to your repo.
- Update the Java version and Gradle tasks as needed for your project.
- See this README section for how to use the workflows.
