# PathwayMatrix

The project formerly known as PathwayMatrix, started in collaboration with the Leist group.

Contains a Vanted extension that evolved from the Master's thesis of Philipp Eberhard.

Project members: Falk Schreiber, Michael Aichem, Karsten Klein, Benjamin Moser, Martin Kern (TBC).

## Documentation
- A comprehensive overview from a programmer's perspective can be found in `/doc/Docs-MA.pdf`. This also includes documentation on the Pinboard feature and pathway scoring.
- Slides at `doc/presentation.pdf`.
- See the project wiki (accessible via the sidebar on GitLab)
- See the [VANTED wiki](https://github.com/LSI-UniKonstanz/vanted/wiki)

## Development

- Latest stable state is on `master`.
- If you want to use IntelliJ IDE (recommended), you can follow [this tutorial](https://github.com/LSI-UniKonstanz/vanted-metabolomics-matrix-addon/blob/main/doc/intellij-setup.md) to set up your project for VANTED plugin development.

### Issues
Known issues are tracked [here](https://www.notion.so/pwaymatrix/Issue-Tracker-a7f79ded9fcd43ebb35617af0cf82464). Email [benjamin.2.moser@uni-konstanz.de](mailto:benjamin.2.moser@uni-konstanz.de) for access. There's an export available in `/docs` but it might not be up to date or readable. 

These tickets additionally often contain any output on work that was already done in investigating them. Additionally, they are tagged with Story Points (estimated effort) and ordered by priority (as determined during a group meeting).

## Getting Started

- You can start VANTED with the Add-On by invoking the `main` method in the class `StartVantedWithAddon`.

## Distribution

- Generate jar-file under use of ant-script "createAdd-on.xml" (in Eclipse right-click and choose "Run As -> Ant Build").

Jar-files can be installed from users by starting VANTED, choosing sidepanel "help -> settings", opening Addon-manager and  choosing "Install Add-on".

## Additional Materials

- **Example data file** that can be used for testing can be found in `/data`

