/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * Utility class, which provides access to images. Images returned by this utility are managed by a ImageRegistry and
 * must not explicitly be disposed, when they are not used anymore.
 *
 * @see {@link ImageRegistry}
 *
 */

// TODO: Copied from org.csstudio.platform.ui. Review is needed.

public final class ImageUtil {
    /**
     * The internal ImageRegistry.
     */
    private ImageRegistry _imageRegistry;

    /**
     * The singleton instance.
     */
    private static ImageUtil _instance;

    /**
     * Hidden constructor.
     */
    private ImageUtil() {
        _imageRegistry = new ImageRegistry();
    }

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance
     */
    public static ImageUtil getInstance() {
        if (_instance == null) {
            _instance = new ImageUtil();
        }

        return _instance;
    }

    /**
     * Gets an ImageDescriptor for an image resource which is supposed to reside in the plugin with the specified
     * pluginId under the specified path.
     *
     * @param pluginId
     *            the ID of the plugin that contains the image resource
     * @param fullPath
     *            the path
     * @return an ImageDescriptor or null
     */
    public ImageDescriptor getImageDescriptorFromFile(final String fullPath) {
        ImageDescriptor descriptor = _imageRegistry.getDescriptor(fullPath);

        if (descriptor == null) {
            try {
                descriptor = ImageDescriptor.createFromURL(new File(fullPath).toURI().toURL());
            } catch (MalformedURLException e) {
                descriptor = null;
            }

            if (descriptor != null) {
                _imageRegistry.put(fullPath, descriptor);
            }
        }

        return descriptor;
    }

    /**
     * Gets an ImageDescriptor for an image resource which is supposed to reside in the plugin with the specified
     * pluginId under the specified path.
     *
     * @param pluginId
     *            the ID of the plugin that contains the image resource
     * @param path
     *            the path
     * @return an ImageDescriptor or null
     */
    public ImageDescriptor getImageDescriptor(final String pluginId,
            final String path) {
        String id = pluginId + "/" + path;
        ImageDescriptor descriptor = _imageRegistry.getDescriptor(id);

        if (descriptor == null) {
            descriptor = imageDescriptorFromPlugin(pluginId, path);

            if (descriptor != null) {
                _imageRegistry.put(id, descriptor);
            }
        }

        return descriptor;
    }

    /**
     * Gets an Image for an image resource which is supposed to reside in the plugin with the specified pluginId under
     * the specified path.
     *
     * @param pluginId
     *            the ID of the plugin that contains the image resource
     * @param path
     *            the path
     * @return an Image or null
     */
    public Image getImage(final String pluginId,
            final String path) {
        String id = pluginId + "/" + path;
        ImageDescriptor descriptor = _imageRegistry.getDescriptor(id);

        if (descriptor == null) {
            descriptor = imageDescriptorFromPlugin(pluginId, path);

            if (descriptor != null) {
                _imageRegistry.put(id, descriptor);
            }
        }

        return _imageRegistry.get(id);
    }

    /**
     * Creates and returns a new image descriptor for an image file located within the specified plug-in.
     * <p>
     * This is a convenience method that simply locates the image file in within the plug-in (no image registries are
     * involved). The path is relative to the root of the plug-in, and takes into account files coming from plug-in
     * fragments. The path may include $arg$ elements. However, the path must not have a leading "." or path separator.
     * Clients should use a path like "icons/mysample.gif" rather than "./icons/mysample.gif" or "/icons/mysample.gif".
     * </p>
     *
     * @param pluginId
     *            the id of the plug-in containing the image file; <code>null</code> is returned if the plug-in does not
     *            exist
     * @param imageFilePath
     *            the relative path of the image file, relative to the root of the plug-in; the path must be legal
     * @return an image descriptor, or <code>null</code> if no image could be found
     * @since 3.0
     */
    private static ImageDescriptor imageDescriptorFromPlugin(
            final String pluginId, final String imageFilePath) {
        if (pluginId == null || imageFilePath == null) {
            throw new IllegalArgumentException();
        }

        // if the bundle is not ready then there is no image
        Bundle bundle = Platform.getBundle(pluginId);

        // look for the image (this will check both the plugin and fragment
        // folders
        URL fullPathString = FileLocator.find(bundle, new Path(imageFilePath),
                null);

        if (fullPathString == null) {
            try {
                fullPathString = new URL(imageFilePath);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        return ImageDescriptor.createFromURL(fullPathString);
    }

}
