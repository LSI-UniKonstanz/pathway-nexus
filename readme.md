# PathwayNexus

The project formerly known as PathwayMatrix, started in collaboration with the Leist group.

Contains a Vanted extension that evolved from the Master's thesis of Philipp Eberhard.

Project members: Falk Schreiber, Michael Aichem, Karsten Klein, Philipp Eberhard, Benjamin Moser, Martin Kern (TBC).

## Documentation
- A comprehensive overview from a programmer's perspective can be found in `/doc/Docs-MA.pdf`. This also includes documentation on the Pinboard feature and pathway scoring.
- Slides at `doc/presentation.pdf`.
- See the project wiki (accessible via the sidebar on GitLab)
- See the [VANTED wiki](https://github.com/LSI-UniKonstanz/vanted/wiki)

## Development

- Latest stable state is on `main`.

## Getting Started

- You can start VANTED with the Add-On by invoking the `main` method in the class `StartVantedWithAddon`.

## Distribution

- Generate jar-file under use of ant-script "createAdd-on.xml" (in Eclipse right-click and choose "Run As -> Ant Build").

Jar-files can be installed from users by starting VANTED, choosing sidepanel "help -> settings", opening Addon-manager and  choosing "Install Add-on".

## Additional Materials

- **Example data file** that can be used for testing can be found on the [PathwayNexus website](https://www.cls.uni-konstanz.de/software/pathway-nexus/getting-started/)

