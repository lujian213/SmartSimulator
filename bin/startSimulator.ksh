HOME_DIR=/C/Users/jl80929/Documents/GitHub/SmartSimulator
HOME_DIR2=/C:/Users/jl80929/Documents/GitHub/SmartSimulator
LIB_DIR=$HOME_DIR/target/lib
CONF_DIR=$HOME_DIR/src/main/resources
JAVA_HOME=/C/applications/jdk1.8.0_71
CLASSPATH=
for i in `ls $LIB_DIR/*.jar`
do
  CLASSPATH=${i}:${CLASSPATH}
done
CLASSPATH=$HOME_DIR/target/SmartSimulator-1.0.1.jar:$CLASSPATH
CLASSPATH=$CONF_DIR:$CLASSPATH
CLASSPATH=$CONF_DIR/log4j.properties:$CLASSPATH
cd $HOME_DIR
$JAVA_HOME/bin/java -classpath $CLASSPATH -Dlog4j.configuration=file://$HOME_DIR2/src/main/resources/log4j.properties -DScript_Home=$HOME_DIR/scripts io.github.lujian213.simulator.SimulatorService scripts


