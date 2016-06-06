/**
 * Main class
 * Created by _ame_ on 09.01.2016 20:52.
 */
public class Run {
static public void FillRandom(int[] array) {
  for (int count=0; count < array.length; count++) {
    array[count] = (int)(Math.random()*100);
  }
}

public static void main(String[] args)
{
  int arraySize= 10000;
  int random[] = new int[arraySize+1];
  FillRandom(random);

  ArrayUtils bubbleClassic = new ArrayUtils(random);
  bubbleClassic.sortBubbleClassic();
  bubbleClassic.results("Bubble Classic, random array");
}
}

