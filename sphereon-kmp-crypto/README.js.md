# Javascript / typescript specifics

# Creating JWKs

Since in Javascript/typescript you are mainly working with objects created on the fly conforming to a type or interface, the library is exposing
methods and interfaces that can be used in Typescript as well. Some examples:

```typescript
// Creating a JWK instance from a JS/JSON object as it is serialized
const jwkJson = Jwk.Companion.fromJsonObject({
    kty: 'EC',
    crv: 'P-256',
    x: 'uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8=',
    y: '29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM='
})

// Creatomg a JWK with some typings
const jwkDto = Jwk.Companion.fromDTO({
    kty: JwaKeyType.EC,
    crv: JwaCurve.P_256,
    x: 'uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8=',
    y: '29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM='
})

// Creating a JWK using a builder
const jwkBuilder = new Jwk.Builder()
    .withKty(JwaKeyType.EC)
    .withCrv(JwaCurve.P_256)
    .withX('uxHN3W6ehp0VWXKaMNie1J82MVJCFZYScau74o17cx8=')
    .withY('29Y5Ey4u5WGWW4MFMKagJPEJiIjzE1UFFZIRhMhqysM=')
    .build()
```

For more about JWKs and how to convert them to Cose Keys and vice versa, see also the [the main README](./README.md#cose-key-and-json-web-keys-jwk)

# X.509 Certificate validation

Below is an example implementation for validating X.509 Certificates in Javascript using a typescript/javascript module
provided by us called @sphereon/ssi-sdk-ext.x509-utils. Of course you can create your own implementation as well

The class implements the kotlin interface below using promises.

```kotlin
interface X509ServiceJS {
    fun <KeyType> verifyCertificateChainJS(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile = X509VerificationProfile.RFC_5280
    ): Promise<X509VerificationResult<KeyType>>

    /**
     * A function returning trusted Certificates in PEM format. Most functions use this as a default in case trusted certificates are not passed in
     * It is up to you to decide if you want to follow that pattern as well. The rest of the code does not explicitly call this method at present
     */
    fun getTrustedCerts(): Array<String>?
}

```

Below is some example code implementing the above interface. If you want to see a real implementation you can have a look in
our [SSI-SDK X509 Service](https://github.com/Sphereon-Opensource/SSI-SDK/blob/12fb17a856f9e7e93c4994ea6bc812d35b43331c/packages/mdl-mdoc/src/functions/index.ts#L21)

```javascript
const {com} = require('@sphereon/kmp-crypto')
const {validateX509CertificateChain} = require('@sphereon/ssi-sdk-ext.x509-utils')

/**
 * Example implementation for Javascript using a package provided by @sphereon/ssi-sdk-ext.x509-utils.
 *
 * This class conforms to the interface exported by the Crypto library above,
 */
class X509ExampleService {

    async verifyCertificateChainJS(chainDER = null, chainPEM = null, trustedCerts = null, verificationProfile = null) {
        // We delegate to the javascript library, which has a very similar interface
        return await validateX509CertificateChain({
            chain: chainDER ?? chainPEM, // The library can handle both, thus we don't care whether it was passed in as PEM or DER.
            trustAnchors: trustedCerts ?? this.getTrustedCerts(), // We use the getTrustedCerts() methods as fallback. Up to you to use something similar or not
            opts: {trustRootWhenNoAnchors: true} // Sphereon module option, that uses the root cert from the chain in case null or undefined trustAnchors are passed in. It basically means to trust the CA in the chain.
        })
    }

    getTrustedCerts() {
        return [sphereonCA]
    }
}

const x509ServiceObjectJS = com.sphereon.crypto.X509ServiceObjectJS
// Register the above class as object with the x509Service, so calls to validate x.509 certificates will be delegated to our class
x509ServiceObjectJS.register(new X509ExampleService())

// Let's call the Javascript service object that get's called from other code as well to see everything works
com.sphereon.crypto.CryptoServiceJS.X509
    .verifyCertificateChainJS(/*we use PEM here*/ null, [walletPEM, sphereonCA], [sphereonCA])
    .then(result => console.log(JSON.stringify(result, null, 2)));


````

Response:

````json
{
  "error": false,
  "critical": false,
  "message": "Certificate chain was valid",
  "verificationTime": "2024-08-07T14:27:11.743Z",
  "certificateChain": [
    {
      "issuer": {
        "dn": {
          "DN": "C=NL,O=Sphereon International B.V.,OU=IT,CN=ca.sphereon.com",
          "attributes": {
            "C": "NL",
            "O": "Sphereon International B.V.",
            "OU": "IT",
            "CN": "ca.sphereon.com"
          }
        }
      },
      "subject": {
        "dn": {
          "DN": "CN=wallet.test.sphereon.com",
          "attributes": {
            "CN": "wallet.test.sphereon.com"
          }
        }
      },
      "publicKeyJWK": {
        "key_ops": [
          "verify"
        ],
        "ext": true,
        "kty": "RSA",
        "n": "xDcYb7J5Wa-PHkTWJPdKr_NF6JJKNpgVhy0FRsGyRyI5CoT1e2gtXs3GjBxrby8IahL5yPsXn3jGFJcNbBYQKhqt1J4asGCzZunUiEHta_GQJuq9Vv0g5WmcsLxJjS69ZXfjC3ZtYlG7Ywv5Kgd8A27tlvE_WM-ozzrCct57_7R4geGIfHxfUe_tfLWYiLfj_msCa82tQ8NRm7lQUysdNClLWdgs5Q",
        "e": "AQAB",
        "alg": "RS256"
      },
      "notBefore": "2024-07-28T20:02:44.000Z",
      "notAfter": "2024-10-26T22:02:44.000Z"
    },
    {
      "issuer": {
        "dn": {
          "DN": "C=NL,O=Sphereon International B.V.,OU=IT,CN=ca.sphereon.com",
          "attributes": {
            "C": "NL",
            "O": "Sphereon International B.V.",
            "OU": "IT",
            "CN": "ca.sphereon.com"
          }
        }
      },
      "subject": {
        "dn": {
          "DN": "C=NL,O=Sphereon International B.V.,OU=IT,CN=ca.sphereon.com",
          "attributes": {
            "C": "NL",
            "O": "Sphereon International B.V.",
            "OU": "IT",
            "CN": "ca.sphereon.com"
          }
        }
      },
      "publicKeyJWK": {
        "key_ops": [
          "verify"
        ],
        "ext": true,
        "kty": "EC",
        "x": "SIDQp4RJI2s5yYIOBrxiwGRROCjBkbCq8vaf3UlSkAw",
        "y": "dRSwvlVFdqdiLXnk2pQqT1vZnDG0I-x-iz2EbdsG0aY",
        "crv": "P-256"
      },
      "notBefore": "2024-07-28T21:26:49.000Z",
      "notAfter": "2034-07-28T21:26:49.000Z"
    }
  ]
}
````

Example certs

```typescript



// Some test certificates to play with. CA is a real CA with CRL
const sphereonCA = "-----BEGIN CERTIFICATE-----\n" +
    "MIICCDCCAa6gAwIBAgITAPMgqwtYzWPBXaobHhxG9iSydTAKBggqhkjOPQQDAjBa\n" +
    "MQswCQYDVQQGEwJOTDEkMCIGA1UECgwbU3BoZXJlb24gSW50ZXJuYXRpb25hbCBC\n" +
    "LlYuMQswCQYDVQQLDAJJVDEYMBYGA1UEAwwPY2Euc3BoZXJlb24uY29tMB4XDTI0\n" +
    "MDcyODIxMjY0OVoXDTM0MDcyODIxMjY0OVowWjELMAkGA1UEBhMCTkwxJDAiBgNV\n" +
    "BAoMG1NwaGVyZW9uIEludGVybmF0aW9uYWwgQi5WLjELMAkGA1UECwwCSVQxGDAW\n" +
    "BgNVBAMMD2NhLnNwaGVyZW9uLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\n" +
    "BEiA0KeESSNrOcmCDga8YsBkUTgowZGwqvL2n91JUpAMdRSwvlVFdqdiLXnk2pQq\n" +
    "T1vZnDG0I+x+iz2EbdsG0aajUzBRMB0GA1UdDgQWBBTnB8pdlVz5yKD+zuNkRR6A\n" +
    "sywywTAOBgNVHQ8BAf8EBAMCAaYwDwYDVR0lBAgwBgYEVR0lADAPBgNVHRMBAf8E\n" +
    "BTADAQH/MAoGCCqGSM49BAMCA0gAMEUCIHH7ie1OAAbff5262rzZVQa8J9zENG8A\n" +
    "QlHHFydMdgaXAiEA1Ib82mhHIYDziE0DDbHEAXOs98al+7dpo8fPGVGTeKI=\n" +
    "-----END CERTIFICATE-----"


const walletPEM = "-----BEGIN CERTIFICATE-----\n" +
    "MIIDwzCCA2mgAwIBAgISKDZBYxEV61yg6xUjrxcTZ17WMAoGCCqGSM49BAMCMFox\n" +
    "CzAJBgNVBAYTAk5MMSQwIgYDVQQKDBtTcGhlcmVvbiBJbnRlcm5hdGlvbmFsIEIu\n" +
    "Vi4xCzAJBgNVBAsMAklUMRgwFgYDVQQDDA9jYS5zcGhlcmVvbi5jb20wHhcNMjQw\n" +
    "NzI4MjAwMjQ0WhcNMjQxMDI2MjIwMjQ0WjAjMSEwHwYDVQQDDBh3YWxsZXQudGVz\n" +
    "dC5zcGhlcmVvbi5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDE\n" +
    "NxhvsnlZr48eRNYk90qv80Xokko2mBWHLQVGwbJHIjkKhPV7aC1ezcaMHGtvLwhq\n" +
    "EvnI+xefeMYUlw1sFhAqGq3UnhqwYLNm6dSIQe1pgHP74nfX06hfgvdGmfZkVxMM\n" +
    "XyxK5gasFg5TuAIsEv8wsqf0vFF2SGKaVFmN5qH4FQvSUtOtJAWQKsee1NSGVkpK\n" +
    "t/POXrG8LidXlpYj17Sh0P8YoFT4DEEj8ZAm6r1W/SDlaZywvEmNLr1ld+MLdm1i\n" +
    "UbtjC/kqB3wDbu2W8T9Yz6jPOsJy3nv/tHiB4Yh8fF9R7+18tZiIt+P+awJrza1D\n" +
    "w1GbuVBTKx00KUtZ2CzlAgMBAAGjggF5MIIBdTAdBgNVHQ4EFgQUuCN6sAJCz64f\n" +
    "CZ3js3ITfKQzFF4wHwYDVR0jBBgwFoAU5wfKXZVc+cig/s7jZEUegLMsMsEwYQYI\n" +
    "KwYBBQUHAQEEVTBTMFEGCCsGAQUFBzAChkVodHRwOi8vZXUuY2VydC5lemNhLmlv\n" +
    "L2NlcnRzL2RhYTFiNGI0LTg1ZmQtNGJhNC1iOTZiLTMzMmFkZDg5OWNlOS5jZXIw\n" +
    "HQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMBMD4GA1UdEQQ3MDWCGHdhbGxl\n" +
    "dC50ZXN0LnNwaGVyZW9uLmNvbYIZZnVua2Uud2FsbGV0LnNwaGVyZW9uLmNvbTAO\n" +
    "BgNVHQ8BAf8EBAMCBLAwYQYDVR0fBFowWDBWoFSgUoZQaHR0cDovL2V1LmNybC5l\n" +
    "emNhLmlvL2NybC8yY2RmN2M1ZS1iOWNkLTQzMTctYmI1Ni0zODZkMjQ0MzgwZTIv\n" +
    "Y2FzcGhlcmVvbmNvbS5jcmwwCgYIKoZIzj0EAwIDSAAwRQIgfY5MD3fWNf8Q0j5C\n" +
    "mYHDHcwOkwygISpMDOh9K5DBBV4CIQCuQ3nToCr/II2WVsAqRXFeZup08fzKLrU2\n" +
    "KZxmdxeoew==\n" +
    "-----END CERTIFICATE-----"
```
