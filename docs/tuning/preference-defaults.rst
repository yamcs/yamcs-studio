Preference Defaults
===================

Most user preferences are linked to the workspace and saved to a folder ``.metadata``. Whenever a user creates a new workspace, the workspace starts with the default preferences.

These default preferences can be modified by adding or modifiying the ``Yamcs Studio.ini`` file in the installation directory of Yamcs Studio. This is often done to ensure that different workstations use similar site-specific configuration.

Some of the more common preferences are documented below.


``org.csstudio.opibuilder/colors.list``
    List of named colors. Entries are separated by semicolons. Each entry is composed as ``NAME@R,G,B``. For example: ``Major@255,0,0;Minor@255,128,0``. You can choose any name, but note that the names ``Major``, ``Minor``, ``Invalid`` and ``Disconnected`` have a special meaning in Yamcs Studio. They are used for common decorations such as out-of-limit indicators.

``org.csstudio.opibuilder/fonts.list``
    List of named fonts. Entries are separated by semicolons. Each entry is composed as ``NAME@FONT-STYLE-SIZE``. For example: ``Header 1@Arial-bold-19;Header 2@Arial-bold-15``. The font should be availabe on the system. Only 'Liberation Sans' (which is the default) is dynamically loaded when it is missing from the system, this is to ensure that the default font settings produce identical displays on all platforms. Note that font size is expressed in points, not pixels.

``org.csstudio.opibuilder/hidden_widgets``
    Hide the specified widgets from the palette. Widgets are mentioned by their id. and separated by the vertical bar character.

``org.csstudio.opibuilder/schema_opi``
    Workspace reference to the active Schema OPI. For example: ``/My Project/schema.opi``

``org.yamcs.studio.core.ui/singleConnectionMode`` ``org.yamcs.studio.core.ui/connectionString``
    When ``singleConnectionMode`` is set to ``true``, Yamcs Studio will not open the Connection Manager window, but will only allow connections to a single Yamcs server defined in the ``connectionString``
