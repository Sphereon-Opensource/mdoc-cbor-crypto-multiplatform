const {Certificate} = require('pkijs');

async function verifyCertificateChain(chainDER = null, chainPEM = null, trustedPEM) {
    console.log(chainDER, chainPEM, trustedPEM);
    const cert =Certificate.fromBER(chainDER[0])
    console.log(JSON.stringify(cert.toJSON()))
    return {
        errors: false,
        critical: true,
        message: "trustedPEM",
        validations: []
    }
}

module.exports.verifyCertificateChain = verifyCertificateChain;
