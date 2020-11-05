#!/bin/bash

now=$(date)
echo $now

> hosts
> logs/stdout
> logs/log

#if [-b build]; then ./template_java/build.sh;

for ((i=1;i<=$1;i++))
do
    echo $i localhost $((11000 + $i))  >> hosts
done

./barrier.py --processes $1 >> ./logs/stdout &

for ((i=1;i<=$1;i++))
do
    > logs/outp$i
    ./template_java/run.sh --id $i --hosts hosts --barrier localhost:11000 --output logs/outp$i >> logs/stdout &
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
