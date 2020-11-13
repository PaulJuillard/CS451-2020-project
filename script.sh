#!/bin/bash

> hosts
> logs/stdout
> logs/log

#if [-b build]; then ./template_java/build.sh;
while getopts ":p:n:b" opt;
do
    case $opt in
        p) 
        P=$OPTARG
        echo "running with $P processes" >&2
        ;;
        n)
        echo "sending $OPTARG messages" >&2
        echo $OPTARG > config
        ;;
        b) 
        ./template_java/build.sh
        ;;
    esac
done

for ((i=1;i<=$P;i++))
do
    echo $i localhost $((11001 + $i))  >> hosts
done

./barrier.py --port 11000 --processes $P >> ./logs/stdout &
./finishedSignal.py --port 11001 -p $P >> ./logs/stdout &

for ((i=1;i<=$P;i++))
do
    > logs/outp$i
    #echo 'coucou'
    ./template_java/run.sh --id $i --hosts hosts --barrier localhost:11000 --signal localhost:11001 --output logs/outp$i config >> logs/stdout & 
done

sleep $((3 + $P))
killall java
sleep 1

cat logs/stdout

echo individual reports

for ((i=1;i<=$P;i++))
do
    #echo output p$i
    wc -l logs/outp$i    
done
