<!--suppress HtmlDeprecatedAttribute -->
<h1 align="center">
  <br>
<a href="https://www.sphereon.com"><img src="https://sphereon.com/content/themes/sphereon/assets/img/logo.svg" alt="Sphereon" width="400"></a>
  <br>Multi platform crypto COSE/JOSE library
  <br>
</h1>

# alpha state

Please be aware that this library still is in alpha state and far from complete. Major changes are likely to happen

# Introduction

This is a multi-platform JOSE/COSE library. The goal is that this library can be used natively on Android, iOS,
apple, linux, windows as well as in JVMs and Javascript/Typescript.

Be aware that this library does not implement actual crypto operations itself. It delegates these to actual callbacks
that are created in different programming languages.

The library does support generic Cose and Jose keys, but only
for transport, not for signing/verification. For these operation we delegate to a service you will need to provide.
Having said that we provide some implementations you could register/use on your platform.

# COSE Key and JSON Web Keys (JWK)

You can convert Cose keys into JWKs and vice versa. On a `Jwk` object you can call to `jwkToCoseKeyCbor()` to get a `CoseKeyCbor` or
`jwkToCoseKeyJson()` to get a `CoseKeyJson`. On a `CoseKeyCbor` and `CoseKeyJson` objects you can call `cborToJwk()` resp `cborToJwkJson()`
respectively.

note: The naming is a bit verbose as some of these are Kotlin extension functions, which would cause name clashes in javascript if we would create
overloads etc.

Example:

```kotlin
val jwk = Jkw(
    kty = JwaKeyType.EC,
    crv = JwaCurve.P_256,
    x = "uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8=",
    y = "29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM="
)
val coseKeyCbor = jwk.jwkToCoseKeyCbor()
val coseKeyJson = jwk.jwkToCoseKeyJson()

// if you feel adventurous; 
// Go from Cbor to Jwk, to Cose Key in JSOM. to JWK, to CoseKeyCbor, to JWK and back to Cbor. The end result should equal the original cbor key
val equalsOriginalCoseKeyCbor = coseKeyCbor.cborToJwk().jwkToCoseKeyJson().jsonToJwk().jwkToCoseKeyCbor().cborToJwk().jwkToCoseKeyCbor()




```

JWK in JSON:

```json
{
  "kty": "EC",
  "crv": "P-256",
  "x": "uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8=",
  "y": "29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM="
}
```

Cbor in HEX:

```text
a401022001215820bb11cddd6e9e869d1559729a30d89ed49f3631524215961271abbbe28d7b731f225820dbd639132e2ee561965b830530a6a024f1098888f313550515921184c86acac3
```

Cbor in diagnostics notation

```text
{
     1: 2,
    -1: 1,
    -2: h'bb11cddd6e9e869d1559729a30d89ed49f3631524215961271abbbe28d7b731f',
    -3: h'dbd639132e2ee561965b830530a6a024f1098888f313550515921184c86acac3',
}
```

The above examples start from a JWK. Of course you can also strart from the Cose Key. Normally the key is part of an object that you deserialized. But
to manually decode a key you can use the example below:

```kotlin


val HEX_ENCODED_CBOR_KEY =
    "a401022001215820bb11cddd6e9e869d1559729a30d89ed49f3631524215961271abbbe28d7b731f225820dbd639132e2ee561965b830530a6a024f1098888f313550515921184c86acac3"
val coseKeyCborFromHex = CoseKeyCbor.cborDecode(HEX_ENCODED_CBOR_KEY.decodeFrom(Encoding.HEX))
```

# Javascript/Typescript

For Javascript / Typescript see [README.js.md](./README.js.md) for more information on how to hookup a Javascript
callback or use a default one we provide. It also shows how you can use a JWK object without having to use the constructors of the JWK object by
leveraging the IJwk interfaces. You can also have a look
at [this example JS source](./src/jsTest/crypto-x509-example-js/index.js) for an example of the validations. If you want to see a real implementation you can have a look in
our [SSI-SDK X509 Service](https://github.com/Sphereon-Opensource/SSI-SDK/blob/12fb17a856f9e7e93c4994ea6bc812d35b43331c/packages/mdl-mdoc/src/functions/index.ts#L21)
