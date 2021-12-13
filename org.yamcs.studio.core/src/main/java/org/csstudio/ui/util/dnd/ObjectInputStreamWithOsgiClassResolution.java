/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util.dnd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.ui.util.ReflectUtil;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * De-serializes java classes looking for the classloaders in the appropriate osgi bundle. Using the normal
 * ObjectInputStream would resolve classes only according to this plugin classloader. This resolves the classes by
 * looking for the class in any plugin whose name is a package parent of the given class.
 */
public class ObjectInputStreamWithOsgiClassResolution extends ObjectInputStream {

    public ObjectInputStreamWithOsgiClassResolution(InputStream in) throws IOException {
        super(in);
    }

    // classes already resolved
    private static Map<String, Class<?>> resolvedClasses = new ConcurrentHashMap<>();

    private static Class<?> findClass(String className, String bundleName) {
        try {
            // Find the matching bundle and try to load the class
            for (var bundle : YamcsPlugin.getDefault().getContext().getBundles()) {
                if (bundle.getSymbolicName().equals(bundleName)) {
                    return bundle.loadClass(className);
                }
            }
        } catch (ClassNotFoundException ex) {
            // class not found
        }
        return null;
    }

    private static Class<?> findClass(String className) {
        // Let's first find a class normally
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            // Not found continue
        }

        // Look inside bundle that have the same name as one of the packages
        // of the class
        var currentBundleName = className;
        while (currentBundleName.lastIndexOf(".") != -1) {
            currentBundleName = currentBundleName.substring(0, currentBundleName.lastIndexOf('.'));
            var clazz = findClass(className, currentBundleName);
            if (clazz != null) {
                return clazz;
            }
        }

        // Can't find it
        return null;
    }

    private static Class<?> getClass(String className) {
        // Look if it's already resolved
        Class<?> clazz = resolvedClasses.get(className);
        if (clazz == null) {
            // If it's an array, resolve the class
            var classNameToResolve = className;
            if (ReflectUtil.isArray(className)) {
                classNameToResolve = ReflectUtil.getComponentType(className);
            }

            // Resolve it
            clazz = findClass(classNameToResolve);

            // If found, cache it
            if (clazz == null) {
                return null;
            }

            resolvedClasses.put(classNameToResolve, clazz);

            if (ReflectUtil.isArray(className)) {
                clazz = Array.newInstance(clazz, 0).getClass();
                resolvedClasses.put(className, clazz);
            }
        }
        return clazz;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        var name = desc.getName();
        Class<?> clazz = getClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }
}
