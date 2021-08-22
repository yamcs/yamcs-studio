Understanding the User Interface
================================

Yamcs Studio is composed out of multiple views that are arranged together in windows. The user has great flexibility in modifying the default arrangement.


Views
-----

Views all share the same user interface organization. On the left you see a tab with the view icon, followed by a title, and then a close icon. On the outer right there are actions to |view-minimize| **Minimize** or |view-maximize| **Maximize** the view. Some views (such as the one in the screenshot) also have a third pull-down icon |view-pulldown| with view-specific actions in it. Most views, though, add dedicated colored icons next to the standard icons. The pull-down menu is used to hide less-often used actions.

.. image:: _images/a-view.png
    :alt: A View
    :align: center

.. note::

    To reopen a view which you closed earlier, or to open another view choose **Window > Show View**.


Views can be resized, moved and stacked. This allows you to customize your workspace to your own personal preference.

When you close Yamcs Studio and later reopen it, your last view and window arrangement will be restored.

If at any time you want to reset your window to the defaults, select **Window > Reset Window Layout...**.

.. note::

    Yamcs Studio stores the information about your view arrangement in a ``.metadata`` folder inside your workspace. This is how it knows how to restore this information through restarts. If you share your workspace with other users through a version control system, you should consider *not* committing this ``.metadata`` folder. This way everybody can have his own preferred arrangement without colliding with each other.


Windows
-------

Yamcs Studio uses two special windows that serve a different purpose:

* :doc:`../display-builder/index`
* :doc:`../display-runner/index`

On a new workspace, Yamcs Studio will by default open the Display Builder window. This includes a default set of views for creating or editing operator displays.

In the top toolbar, you find a play button which opens the Display Runner window. This window includes a different set of windows that put the focus on operations. In this window, displays open in runtime mode.

In the top toolbar of the Display Runner there is a pickaxe button which allows you to open or bring to the front the Display Builder window.


.. |view-maximize| image:: _images/view-maximize.png
.. |view-minimize| image:: _images/view-minimize.png
.. |view-pulldown| image:: _images/view-pulldown.png
