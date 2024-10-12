#!/bin/sh

cd "$(dirname $0)"

echo "Generating local certificates for development..."
echo ""

# Create CA key and certificate
openssl genrsa --out ca.priv 2048

openssl req -x509 -new -nodes -sha256 -days 1825 -key ca.priv \
        -out ca.crt -subj "/C=US/O=jasonsjones/CN=ca"

# Create server key and certificate
openssl genrsa -out sandbox.priv 2048

openssl req -new -key sandbox.priv -out sandbox.csr \
        -subj "/C=US/O=jasonsjones/CN=jasonsjones.com"

cat <<EOF >sandbox.ext
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
DNS.2 = jasonsjones.com
DNS.3 = www.jasonsjones.com
EOF

openssl x509 -req -in sandbox.csr -CA ca.crt -CAkey ca.priv \
        -out sandbox.crt -CAcreateserial  -days 1825 -sha256 \
        -extfile sandbox.ext

openssl pkcs12 -export -in sandbox.crt -inkey sandbox.priv \
        -out sandbox.p12 -certfile sandbox.crt \
        -password pass:changeit -name sandbox

openssl pkcs12 -in sandbox.p12 -out sandbox.pub \
        -clcerts -nokeys -passin pass:changeit

openssl pkcs12 -in sandbox.p12 -out sandbox.pem \
        -nodes -passin pass:changeit

