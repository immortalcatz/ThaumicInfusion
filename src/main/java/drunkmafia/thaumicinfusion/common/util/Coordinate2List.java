package drunkmafia.thaumicinfusion.common.util;

import java.lang.reflect.Array;

/**
 * This works based on a graph design to allow for negative coordinates:
 *
 *        X  | Z
 *      -----------
 *       Pos | Neg
 *       Neg | Pos
 *       Pos | Pos
 *       Neg | Neg
 *
 * Making it ideal for coordinate based lists, it gives a faster lookup time than lists and maps
 * since you need to know the coordinates of the element you want to look up.
 *
 * This also works like the list, it will automatically resize itself to fit elements. Run the clean
 * every so often to trim the arrays.
 *
 * @author TheDrunkMafia
 */
@SuppressWarnings("unchecked")
public class Coordinate2List<T> {

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     */
    private Integer[][] posPos, negNeg, posNeg, negPos;

    private int initalSize;

    private T[] elementData;

    /**
     * The margin which the arrays are shifted by when being resized
     */
    private int shitMargin;
    private Class<T> tClass;

    /**
     * @param initalSize of arrays, this will increase put Times
     * @param tClass used to create the arrays
     * @param shitMargin used to push the array size up to give some leeway
     * @param maxAttempts used to ensure that the list does not cause an infinite loop when adding data
     */
    public Coordinate2List(Class<T> tClass, int initalSize, int shitMargin, int maxAttempts){
        if(tClass == null || initalSize < 0 || shitMargin < 0)
            throw new IllegalArgumentException("Bad Arguments, Failed to create list. Class: " + tClass + " Size: " + initalSize + " Shift: " + shitMargin);

        this.tClass = tClass;
        this.shitMargin = shitMargin;
        this.initalSize = initalSize;
        this.maxAttempts = maxAttempts;

        removeAll();
    }

    public Coordinate2List(Class<T> tClass){
        this(tClass, 1, 100, 5);
    }

    public Coordinate2List(){
        this((Class<T>) Object.class);
    }

    /**
     *
     * Puts the element at a certain X & Z position
     *
     * @param element to be added to list
     * @param x pos
     * @param z pos
     */
    public void set(T element, int x, int z){
        boolean xPos = x > 0, zPos = z > 0;
        Integer[][] array = getArray(xPos, zPos);

        x = (x < 0) ? -x : x;
        z = (z < 0) ? -z : z;

        if(x >= array.length) array = changeArraySize(array, x + shitMargin, new Integer[x + shitMargin], Integer.class);
        if(array[x] != null && z >= array[x].length) array[x] = changeArraySize(array[x], z + shitMargin, Integer.class);

        if(element != null)
            array[x][z] = addElement(element);
        else{
            elementData[array[x][z]] = null;
            array[x][z] = null;
        }

        setArray(xPos, zPos, array);
    }

    private int attempt = 0, maxAttempts;

    public int addElement(T element){
        if(attempt++ > maxAttempts)
            throw new IllegalArgumentException("Exceeded max amount of attempts to add data to list");

        for(int i = 0; i < elementData.length; i++){
            if(elementData[i] == null){
                elementData[i] = element;
                attempt = 0;
                return i;
            }
        }
        elementData = changeArraySize(elementData, elementData.length + shitMargin + 1, tClass);
        return addElement(element);
    }

    /**
     * Directly accesses the array at the specified look up, nominal lookup due to this
     * @param x pos
     * @param z pos
     * @return Element at that position, can be null
     */
    public T get(int x, int z){
        Integer[][] array = getArray(x > 0, z > 0);

        x = (x < 0) ? -x : x;
        z = (z < 0) ? -z : z;
        return (x < array.length && z < array[x].length && array[x][z] != null) ? elementData[array[x][z]] : null;
    }

    private Integer[][] getArray(boolean xPos, boolean zPos){
        return xPos && zPos ? posPos : !xPos && zPos ? negPos : xPos ? posNeg : negNeg;
    }

    private void setArray(boolean xPos, boolean zPos, Integer[][] array){
        if(xPos && zPos) posPos = array;
        if(!xPos && zPos) negPos = array;
        if(xPos && !zPos) posNeg = array;
        if(!xPos && !zPos) negNeg = array;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " Type: " + tClass.getSimpleName();
    }

    /**
     * Recreates the list, removing all stored objects
     */
    public void removeAll(){
        elementData = (T[]) Array.newInstance(tClass, initalSize, initalSize);

        posPos = new Integer[initalSize][initalSize];
        negNeg = new Integer[initalSize][initalSize];
        posNeg = new Integer[initalSize][initalSize];
        negPos = new Integer[initalSize][initalSize];
    }

    /**
     * Resize a 1 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected <E>E[] changeArraySize(E[] old, int newSize, Class<E> type){
        E[] newArray = (E[]) Array.newInstance(type, newSize);
        System.arraycopy(old, 0, newArray, 0, old.length);
        return newArray;
    }
    /**
     * Resize a 2 dimensional array
     * @param old array
     * @param newSize of the array to create
     * @return New array with old elements and new size
     */
    protected <E>E[][] changeArraySize(E[][] old, int newSize, E[] defaultVal, Class<E> type){
        E[][] newArray = (E[][]) Array.newInstance(type, newSize, newSize);
        System.arraycopy(old, 0, newArray, 0, old.length);
        for(int i = 0; i < newArray.length; i++)
            if(newArray[i] == null) newArray[i] = defaultVal;
        return newArray;
    }
}
