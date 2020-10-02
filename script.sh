#!/bin/bash

now=$(date)
echo $now

> hosts
> logs/stdout
> logs/log
for ((i=1; i<=$1; i++))
do
    echo $i localhost 1100$i >> hosts
done

./barrier.py --processes $1 >> ./logs/stdout &

for ((i=1; i<=$1; i++))
do
    ./template_java/run.sh --id $i --hosts hosts --barrier localhost:11000 --output logs/log >> logs/stdout &
done

sleep 4
killall java

cat logs/stdout