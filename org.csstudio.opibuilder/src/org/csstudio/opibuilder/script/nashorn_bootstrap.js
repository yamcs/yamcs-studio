// Trimmed and fixed version of mozilla_compat.js included in Nashorn.
// In the original version, importPackage contains a memory leak.

// JavaAdapter
Object.defineProperty(this, "JavaAdapter", {
    configurable: true, enumerable: false, writable: true,
    value: function() {
        if (arguments.length < 2) {
            throw new TypeError("JavaAdapter requires atleast two arguments");
        }

        var types = Array.prototype.slice.call(arguments, 0, arguments.length - 1);
        var NewType = Java.extend.apply(Java, types);
        return new NewType(arguments[arguments.length - 1]);
    }
});


// importPackage
// avoid unnecessary chaining of __noSuchProperty__ again
// in case user loads this script more than once.
if (typeof importPackage == 'undefined') {

Object.defineProperty(this, "importPackage", {
    configurable: true, enumerable: false, writable: true,
    value: (function() {
        var _packages = [];
        var global = this;
        var oldNoSuchProperty = global.__noSuchProperty__;
        var __noSuchProperty__ = function(name) {
            'use strict';
            for (var i in _packages) {
                try {
                    var type = Java.type(_packages[i] + "." + name);
                    global[name] = type;
                    return type;
                } catch (e) {}
            }

            if (oldNoSuchProperty) {
                return oldNoSuchProperty.call(this, name);
            } else {
                if (this === undefined) {
                    throw new ReferenceError(name + " is not defined");
                } else {
                    return undefined;
                }
            }
        }

        Object.defineProperty(global, "__noSuchProperty__", {
            writable: true, configurable: true, enumerable: false,
            value: __noSuchProperty__
        });

        var prefix = "[JavaPackage ";
        return function() {
            for (var i in arguments) {
                var pkgName = arguments[i];
                if ((typeof pkgName) != 'string') {
                    pkgName = String(pkgName);
                    // extract name from JavaPackage object
                    if (pkgName.startsWith(prefix)) {
                        pkgName = pkgName.substring(prefix.length, pkgName.length - 1);
                    }
                }
                if (_packages.indexOf(pkgName) === -1) { // Check added by FDI (avoid leak)
                    _packages.push(pkgName);
                }
            }
        }
    })()
});

}

// Rhino: global.importClass
Object.defineProperty(this, "importClass", {
    configurable: true, enumerable: false, writable: true,
    value: function() {
        for (var arg in arguments) {
            var clazz = arguments[arg];
            if (Java.isType(clazz)) {
                var className = Java.typeName(clazz);
                var simpleName = className.substring(className.lastIndexOf('.') + 1);
                this[simpleName] = clazz;
            } else {
                throw new TypeError(clazz + " is not a Java class");
            }
        }
    }
});
