<!--suppress HtmlDeprecatedAttribute -->
<h1 align="center">
  <br>
<a href="https://www.sphereon.com"><img src="https://sphereon.com/content/themes/sphereon/assets/img/logo.svg" alt="Sphereon" width="400"></a>
  <br>Multi platform mDL/mdoc, crypto and CBOR
  <br>
</h1>

# alpha state

Please be aware that this library still is in alpha state and far from complete. Major changes are likely to happen

# Introduction

This is a multi-platform mDL/mdoc and CBOR library. The goal is that this library can be used natively on Android, iOS,
apple, linux, windows as well as in JVMs and Javascript/Typescript.

Certain functions are delegate to native platform functions and you will have to provide these yourself. We have done
this to not make to many assumptions on crypto libraries. The library does support generic Cose and Jose keys, but only
for transport, not for signing/verification. For these operation we delegate to a service you will need to provide.
Having said that we provide some implementations you could register/use on your platform.

# CBOR

A multi-platform CBOR library. The goal is that this library is to be able to work with CBOR data. It can be used natively on Android, iOS,
apple, linux, windows as well as in JVMs and Javascript/Typescript.

# Crypto JOSE/COSE

A multi-platform JOSE/COSE library. The goal is that this library is to be able to convert COSE/CBOR keys and JOSE JWKs and other primitives into
one-another. The library also has support for X.509 Certificate chain validations. It can be used natively on Android, iOS,
apple, linux, windows as well as in JVMs and Javascript/Typescript. See
the [crypto README](./sphereon-kmp-crypto/README.md) for more information.

# MDL and Mdoc

# CBOR and Json views explained

You will notice that we have primitive CBOR objects like `CborString`, `CborInt`, representing their programming
language
primitive object counterparts `String`, `Int` etc. These are also handy outside of the MDL/Mdoc scope for general CBOR
processing. Then we also have more complex CBOR objects, like for
instance `DrivingPrivelegesCbor`, representing ISO/MDL Driving Privileges in CBOR format and using `Cbor` primitive
objects and
`Cbor` complex objects internally, also sometimes using delegation structures to ease serialization. At the same time we
also have
a `DrivingPrivilegesJson` object, which contains programming language
primitives and other Json objects containing simple primitives/objects.

Most complex objects have these dual CBOR/JSON views. Strictly speaking we could have skipped the
dual `Cbor` and `Json` view approach as the serialization solution would be able to handle it. Introducing the CBOR
objects
helps us ease the non-reflection based approach of this serialization solution.
The current serialization solution has support for our
main target platforms (
Kotlin, JVM/Java, iOS, Android, Javascript/Typescript/React-Native )

However that is not main reason for the split. We wanted to:

- have separate CBOR and COSE cross platform support not tied to MDL/Mdocs in the future.
- be able for users to decide whether they want to do JSON serialization for instance when incorporating this
  library in a JSON/REST based API. The Json counterparts are directly serializable to JSON. Obviously MDL/Mdoc
  interactions are done almost entirely in CBOR (some exceptions for server based retrieval/JSON)
- have strongly typed objects, including CBOR/CDDL information coming from CBOR interactions.
- have simple objects for developers to deal with
- ensure that JSON serialization and CBOR serialization cannot be in eachothers way.

`Cbor` and `Json` objects can be directly converted into their counterparts, by simply calling `toCbor()`
and `toJson()` respectively. The complex `Cbor` objects also have builders, which can be instantiated using `Cbor`
primitives
and `Cbor` complex objects, but also by calling the builder methods that are more focused on simple programming language
primitives and
objects. This allows developers to create CBOR objects easily and leaves them the choice to construct a `Json`
object and calling `toCbor()` on it or using the builder on the `Cbor` object.
Construction of the Json objects should be straightforward especially with programming languages supporting named
parameters; Since we do not know whether all (future) targets will have this easy approach, and because builders are
also a popular way to construct objects, we decided to include builders for almost all CBOR complex objects.
Most objects coming from a MDL/Mdoc interaction thus will be in the CBOR object format. Converting these simply means
calling `toJson()` on them to make them easier for developers to deal with. So to conclude:

- Produced by MDL/Mdoc interactions:
    - `<ObjectName>Cbor` Object -> call `toJson()`
- to be consumed by MDL/Mdoc:
    - `<ObjectName>Json` object -> Call `toCbor()`, or
    - `<ObjectName>Cbor` object -> Call the builder, using `Json` and primitive arguments or `Cbor` names parameters

There is one caveat for certain JSON properties. Whenever we use polymorphic values, there typically is a need to
provide the CDDL value as well. This has to do with the fact that we otherwise would lose information when going from
JSON to CBOR. For instance dates in CBOR can be expressed using strings and numbers. Then there are long and short
dates. So whenever we encounter a JSON number or string, we would not know how to map that to Cbor. Of course this is
only
applicable to where polymorphism is possible for a property. In these cases the CDDL value is made mandatory. These are
the only places where the CBOR types seep into the JSON types.
