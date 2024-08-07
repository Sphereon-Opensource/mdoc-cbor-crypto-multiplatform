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

# Javascript/Typescript

For Javascript / Typescript see [README.js.md](./README.js.md) for more information on how to hookup a Javascript
callback or use a default one we provide. You can also have a look
at [this example JS source](./src/jsTest/crypto-x509-example-js/index.js)
