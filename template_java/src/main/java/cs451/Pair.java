package cs451;

import java.util.Objects;

public class Pair<X, Y> { 


  public final X _1; 
  public final Y _2; 

  public Pair(X x, Y y) { 
    this._1 = x; 
    this._2 = y; 
  } 

  @Override
  public boolean equals(Object o){
      if(o == null || o.getClass() != this.getClass()){
            return false;
      }
      else{
            Pair<?, ?> other = (Pair<?, ?>) o;
            return other._1.equals(this._1) && other._2.equals(this._2);
      }
  }

  @Override
  public int hashCode(){
      return Objects.hash(this._1, this._2);
  }

  @Override
  public String toString(){
    return "(" + this._1.toString() + "," + this._2.toString() + ")";
  }
} 