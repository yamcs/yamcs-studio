Formulas
========

PVs can be combined with mathematical expressions. Formulas always start with ``=`` followed by a formula expression. Note that any referenced PVs must be wrapped with single quotes.

Example PV Names:

* ``=3*'loc://foo(2)'``
* ``=3.14``
* ``=log('loc://foo(2)')``

Supported formulas include:

* abs(a)
* acos(a)
* asin(a)
* atan(a)
* ceil(a)
* cos(a)
* cosh(a)
* exp(a)
* expm1(a)
* floor(a)
* log(a)
* log10(a)
* round(a)
* sin(a)
* sinh(a)
* sqrt(a)
* tan(a)
* tanh(a)
* toDegrees(a)
* toRadians(a)
* atan2(a, b)
* hypot(a, b)
* pow(a, b)
* min(a, b, c, d, e)
* max(a, b, c, d, e)
