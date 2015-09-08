/**
 * Created by _ame_ on 09.07.2015 1:06.
 */

outer: {
  inner: {
    console.log("Я внутри произвольного блока");
    if (true) {
      break outer;
    }
  }
  console.log("Эта строчка никогда не выполнится");
}

console.log(function(s,i){return s.charAt(i)+','+s.charAt(i-1)+'.'}("123",2));
console.log(((s,i) => s.charAt(i)+','+s.charAt(i-1)+'.')("123",2));
