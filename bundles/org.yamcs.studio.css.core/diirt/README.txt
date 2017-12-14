The 4.5 CS-Studio/DIIRT configuration has changed compared to 4.3.
It is no longer possible to add a default datasource other than
a handful of CS-Studio created ones.

Further it is now necessary to add jca bundle and its configuration. Omitting
to do so generates an exception.

The reason for all this appears to be that bundle XML files get
re-serialized into the workspace preferences. The code responsible
for this appears not so friendly for customizations.

Follow:

https://github.com/ControlSystemStudio/cs-studio/issues/2196
https://github.com/ControlSystemStudio/cs-studio/blob/master/core/diirt/diirt-plugins/org.csstudio.diirt.util.core.preferences
