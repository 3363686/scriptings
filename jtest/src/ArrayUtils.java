/**
 * From: http://habrahabr.ru/post/274493/
 * Created by _ame_ on 09.01.2016 20:41.
 */

import java.util.Arrays;

import static java.lang.System.out;

public class ArrayUtils {

final private int[] array;
final private int[] sortedArray;
long switchCount, compareCount, timeAmount;

public ArrayUtils(int[] array) {
  this.array = array;
  this.sortedArray = Arrays.copyOf(array, array.length);
  Arrays.sort(sortedArray);
}
boolean validate(){
  for (int i=0; i < array.length; i++){
    if (array[i] != sortedArray[i])
      return false;
  }
  return true;
}
public void sortBubbleClassic() {
  int currentPosition;
  int maxPosition;
  int temp;
  switchCount=0;
  compareCount=0;
  timeAmount = System.nanoTime();

  for (maxPosition=array.length - 1; maxPosition >= 0;maxPosition--)	{
    for (currentPosition = 0; currentPosition < maxPosition; currentPosition++) {
      compareCount++;
      if (array[currentPosition] > array[currentPosition+1]) {
        temp = array[currentPosition];
        array[currentPosition] = array[currentPosition+1];
        array[currentPosition+1] = temp;
        switchCount++;
      }
    }
  }
  timeAmount = System.nanoTime() - timeAmount;
  assert(validate());
  return;
}
public void results (String hdr){
  out.printf( "%40s: Cmp%12d, Sw%12d, Time%12d nsec\n",
   hdr, compareCount, switchCount, timeAmount );
}
}
