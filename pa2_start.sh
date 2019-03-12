CLASSES='~/CS455/HW2/PC/build/classes/java/main/'
SCRIPT="cd $CLASSES; java main.java.cs455.scaling.client.Client santa-fe 5003 2"
for ((j=1;j<=$1;j++));
do
    COMMAND='gnome-terminal'
    for i in `cat machine_list`
    do
        echo 'logging into '$i
        OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
        COMMAND+=" $OPTION"
    done
    eval $COMMAND &
done

# eval $SCRIPT
