const {Certificate, CertificateChainValidationEngine, setEngine} = require('pkijs');
const {com} = require('@sphereon/kmp-crypto')
const pvutils = require('pvutils')


class X509MockService {

    async verifyCertificateChainJS(chainDER = null, chainPEM = null, trustedCerts = null, verificationProfile = null) {
        const certs = chainPEM.map(pem => decodePemToCert(pem))
        const chainEngine = new CertificateChainValidationEngine({
            certs,
            crls: [],
            // crls: [crl1],
            // ocsps: [ocsp1],
            // checkDate: new Date("2015-07-13"), // optional
            trustedCerts: (trustedCerts ?? this.getTrustedCerts()).map(pem => decodePemToCert(pem)),
        });
        setEngine("global", crypto)

        const verification = await chainEngine.verify()
        const message = verification.resultMessage && verification.resultMessage.length > 0 ? verification.resultMessage : `Chain validation was ${verification.result ? '' : 'un'}successful`
        console.log(JSON.stringify(verification, null, 2))
        return {
            error: !verification.result,
            critical: true,
            message
        }
    }

    getTrustedCerts() {
        return [sphereonCA]
    }
}

function decodePemToCert(pem) {
    if (typeof pem !== 'string') {
        throw new Error('Expected PEM as string');
    }

    // Load certificate in PEM encoding (base64 encoded DER)
    const b64 = pem.replace(/(-----(BEGIN|END) CERTIFICATE-----|[\n\r])/g, '');
    console.log('----\r\n' + b64 + '\r\n----');

    // And massage the cert into a BER encoded one
    const ber = pvutils.stringToArrayBuffer(pvutils.fromBase64(b64))
    return Certificate.fromBER(ber);
}

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


const x509ServiceObjectJS = com.sphereon.crypto.X509ServiceObjectJS
x509ServiceObjectJS.register(new X509MockService())
console.log(com.sphereon.crypto.CryptoServiceJS.X509.verifyCertificateChainJS(null, [walletPEM, sphereonCA], [sphereonCA]))

module.exports.x509ServiceObjectJS = x509ServiceObjectJS;
