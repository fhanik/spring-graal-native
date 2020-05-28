GRAALVM_BRANCH=master
GRAAL_HOME=/opt/graalvm/graalvm-$GRAALVM_BRANCH
JVMCI_URL=https://github.com/graalvm/graal-jvmci-8/releases/download/jvmci-20.1-b02/openjdk-8u252+09-jvmci-20.1-b02-linux-amd64.tar.gz
JAVA_HOME=$GRAAL_HOME/openjdk-jvmci
PATH=$GRAAL_HOME/mx-master:$JAVA_HOME/bin:$PATH


mkdir -p $GRAAL_HOME
rm -rf $GRAAL_HOME/*


sudo apt-get -y install git curl build-essential python unzip zlib1g-dev

cd $GRAAL_HOME \
 && rm -rf * \
 && curl -LO https://github.com/graalvm/mx/archive/master.zip \
 && unzip master.zip \
 && rm master.zip \
 && curl -L $JVMCI_URL | tar -xz \
 && mv *jdk* openjdk-jvmci \
 && git clone --single-branch --branch $GRAALVM_BRANCH https://github.com/oracle/graal.git \
 && cd graal/vm \
 && mx --disable-polyglot --disable-libpolyglot --dynamicimports /substratevm build

export GRAALVM_HOME=$GRAAL_HOME/graal/vm/latest_graalvm_home
export PATH=$GRAALVM_HOME/bin:$PATH
export JAVA_HOME=$GRAALVM_HOME

