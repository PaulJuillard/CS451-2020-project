#!/bin/bash

> hosts
> logs/stdout
> logs/log

#if [-b build]; then ./template_java/build.sh;

for ((i=1;i<=$1;i++))
do
    echo $i localhost $((11001 + $i))  >> hosts
done

./barrier.py --port 11000 --processes $1 >> ./logs/stdout &
./finishedSignal.py --port 11001 -p $1 >> ./logs/stdout &

for ((i=1;i<=$1;i++))
do
    > logs/outp$i
    ./template_java/run.sh --id $i --hosts hosts --barrier localhost:11999 --output logs/outp$i >> logs/stdout &
done

sleep 3
killall java

cat logs/stdout

echo individual reports

for ((i=1;i<=$1;i++))
do
    #echo output p$i
    wc -l logs/outp$i    
done
