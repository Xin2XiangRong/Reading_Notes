## Effective Java

### 一.创建和销毁对象

#### 1.考虑用静态工厂方法代替构造器

优点：

1. 有名称
2. 不必每次调用的时候都创建一个对象 
3. 可以返回原返回类型的任何子类型的对象   服务提供者框架模式  服务访问Api可以利用适配器模式返回比提供者需要的更丰富的服务接口

```java
//Service provider framework sketch
//service interface
public interface Service {
    ...//Service-specific methods go here
}
//Service provider interface
public interface Provider {
    Service new Service();
}
//Noninstantiable class for service registration and access
public class Service {
    private Service() {} //Prevents instantiation
    //Maps service names to services
    private static final Map<String, Provider> providers = new ConcurrentHashMap<String, Provider>();
    public static final String DEFAULT_PROVIDER_NAME = "DEF";
    //Provider registration API
    public static void registerDefaultProvider(Provider p) {
        registerProvider(DEFAULT_PROVIDER_NAME, p);
    }
    public static void registerProvider(String name, Provider p) {
        providers.put(name, p);
    }
    
    //service access api
    public static Service newInstance() {
        return newInstance(DEFAULT_PROVIDER_NAME);
    }
    public static Service newInstance(String name) {
        Provider p= provider.get(name);
        if(p==null) {
            throw new IllegalArgumentException("no provider registered with name:"+name);
        }
        return p.newInstance();
    }
}
```

4. 在创建参数化类型实例的时候，使代码变得更加简洁

   ```java
   //对于
   Map<String, List<String>> m = new HashMap<String, List<String>>();
   //如果存在以下静态方法
   public static <K, V> HashMap<K, V> newInstance() {
       return new HashMap<K, V>();
   }
   //就可以写成
   Map<String, List<String>> m = HashMap.newInstance();
   ```

   缺点：1.类如果不含公有的或者受保护的构造器，就不能被子类化  2.与其他的静态方法实际上没有任何区别

   静态工厂方法的一些惯用名称： `valueOf()  of ()  getInstance()  newInstance()  getType()   newType() ` 

####  2.遇到多个构造器参数时要考虑用构建器

当遇到有多个参数的构造方法时，一般采用下面两种方式

1.采用重叠构造器模式

~~~~java
public class NutritionFacts {
    private final int servingSize;  //required
    private final int servings;  //required
    private final int calories;	//optional
    private final int fat;  //optional
    private final int sodium;  //optional
    private final int carbohydrate;  //optiional
    
    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }
    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }
    ......
    public NutrionFacts(int servingSize, int servings, int calories, int fat,
                        int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
~~~~

2.JavaBeans模式，在这种模式下，调用一个无参构造器来创建对象，然后调用setter方法来设置每个参数

JavaBeans模式的构造过程被分到了几个调用之中，在构造过程中javabean可能处于不一致的状态。类无法仅仅通过检验构造器参数的有效性来保证一致性；另外，javabean模式阻止了把类做成不可变的可能性，这就需要付出额外的努力来确保它的线程安全

3.Builder模式：既能保证像重叠构造器模式那样的安全性，也能保证像javaBean模式那么好的可读性。不直接生成想要的对象，而是让客户端利用所有必要的参数调用构造器（或者静态工厂），得到一个builder对象。然后客户端在builder对象上调用类似于setter方法，来设置每个相关的可选参数。最后，客户端调用无参的build方法来生成不可变的对象。

~~~~java
public calss NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;
    
    public static class Builder {
        //required paramters
        private final int servingSize;
        private final int servings;
        //optional parameters --initialized to default values
        private int calories = 0;
        private int fat = 0;
        private int carbohydrate = 0;
        private int sodium = 0;
        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }
        public Builder calories(int val) {
            calories = val;
            return this;
        }
        public Builder fat(int val) {
            fat = val;
            return this;
        }
        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }
        public Builder sodium(int val) {
            sodium = val;
            return this;
        }
        public NutritionFacts builder() {
            return new NutritionFacts(this);
        }
        
        private NutritionFacts(Builder builder) {
            servingSize = builder.servingSize;
            servings = builder.servings;
            calories = builder.calories;
            fat= builder.fat;
            sodium = builder.sodium;
            carbohydrate = builder.carbohudrate
        }
    }
}

//客户端代码
NutritionFacts cacaCala = new NutritonFacts.Builder(240,8)
    .calories(100).sodium(35).carbohydrate(27).build();
~~~~

#### 3.用私有构造器或者枚举类型强化Singleton属性

#### 4.通过私有构造器强化不可实例化的能力

对于只包含静态方法和静态域的类，不希望被实例化，可以编写私有的构造方法。这种习惯用法有一个副作用：使得一个类不能被子类化。所有的构造器都必须显式或隐式的调用超类构造器，在这种情形下，子类就没有可访问的超类构造器可调用了。

#### 5.避免创建不必要的对象

对于多次使用到对象，可以从局部变量改为final静态域；要优先使用基本类型而不是装箱基本类型，要当心无意识的自动装箱

不要错误的认为本条目所介绍的内容暗示这“创建对象的代价非常昂贵，我们应该要尽可能地避免创建对象”。相反，由于小对象的构造器只做很少量的显式工作，所以，小对象的创建和回收动作是非常廉价的，特别是在现代的JVM实际上更是如此。通过创建附加的对象，提升程序的清晰性、简洁性和功能性，这通常是件好事。

#### 6.消除过期的对象引用

避免内存泄露

#### 7.避免使用终结方法

### 二.对于所有对象都通用的方法

#### 8.覆盖equals时请遵守通用约定

自反性、对称性、传递性、一致性、非空性

#### 9.覆盖equals时总要覆盖hashCode

Object规范：1）在应用程序的执行期间，只要对象的equals方法的比较操作所用到的信息没有被修改，那么对这同一个对象调用多次，hashCode方法都必须始终如一地返回同一个整数；  2）如果两个对象根据equals(Object)方法比较是相等的，那么调用这两个对象中任意一个对象的hashCode方法都必须产生同样的整数结果。

如果只是重写了equals方法，而没有重写hashCode方法，从而导致两个相等的实例具有不相等的散列码，违反了hashCode的约定。put方法把电话号码对象存放在一个散列桶中，get方法却在另一个散列桶中进行查找。即使这两个实例正好被放到同一个散列桶中，get方法也必定会返回null，因为HashMap有一项优化，可以将与每个项相关联的散列码缓存起来，如果散列码不匹配，也不必检验对象的等同性

#### 10.始终要覆盖toString

#### 11.谨慎地覆盖clone

如果一个类实现了Cloneable，Obejct的clone方法就返回该对象的逐域拷贝，否则就会抛出CloneNotSupportedException异常

clone()的一般含义是，对于任何对象x，表达式`x.clone() != x   //true `，并且·`x.clone().getClass() == x.getClass()  //true`，但这些都不是绝对的要求。通常情况下，`x.clone().equals(x)  //true`。

如果对象中包含的域引用了可变的对象，使用简单的`super.clone()` 实现可能会导致灾难性的后果。列如对于以下Stack类：

~~~~java
public class Stack implements Clonable{
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null;
        return result;
    }
    //ensure space for at least one more element
    private void ensureCapacity() {
        if (elements.length == size) {
            ellements = Arrays.copyOf(elements, 2*size+1);
        }
    }
    
    //如果仅仅使用super.clone(),elements域将引用与原始Stack实例相同的数组，即复制的是其地址，在做值修改时，会互相影响
    //拷贝栈的内部信息
    @Override
    public Stack clone() {
        try {
            Stack result = (Stack) super.clone();
            result.elements = elements.clone();
            return result;
        } catch(CloneNotSupportedException e) {
            throw new AssertinError();
        }
    }
}
~~~~

递归的调用clone有时还不够。比如，类中的域是引用对象的数组

~~~~java
public class HashTable impliments Cloneable {
    private Entry[] buckets = new Entry[];
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    ...//remainder omitted
}
~~~~

假设仅仅递归地克隆这个散列数组，就像对Stack类所做的那样

~~~java
@Override
public HashTable clone() {
	try {
        HashTable result = (HashTable) super.clone();
        result.buckets = buckets.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
~~~

虽然被克隆对象有它自己的散列桶数组，但是，这个数组引用的链表与原始对象是一样的，从而很容易引起克隆对象和原始对象中不确定的行为。为了修正这个问题，必须单独的拷贝并组成每个桶的链表

~~~java
public class HashTable impliments Cloneable {
    private Entry[] buckets = new Entry[];
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        Entry deepCopy() {
            return new Entry(key, value, next==null ? null:next.deepCopy());
        }
    }
    @Override
    public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = new Entry[buckets.length];
            for(int i=0;i<buckets.length;i++) {
                if(bucket[i]!=null)
                    result.buckets[i]=buckets[i].deepCopy();
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    ...//remainder omitted
}
~~~

Entry类中的深度拷贝方法递归地调用它自身，以便拷贝整个链表；针对列表中的每个元素，它都要消耗一段栈空间，如果链表比较长，这很容易导致栈溢出。为了避免发生这种情况，可以在deepCopy中用迭代代替递归

~~~java
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for(Entry p=result;p.next!=null;p=p.next) {
        p.next=new Entry(p.next.key, p.next.value, p.next.next);
    }
    return result;
}
~~~

克隆复杂对象的最后一种办法是，先调用supeer.clone()，然后把结果对象中的所有域都设置成他们的空白状态，然后调用高层的方法来重新产生对象的状态。对于上面的HashTable例子中，buckets域将被初始化为一个新的散列桶数组，然后，对于正在被克隆的散列表中的每一个键-值映射，都调用put(key, value)方法

#### 12.考虑实现Comparable接口

compareTo方法的通用约定与equals方法的相似：将这个对象与指定的对象进行比较。当该对象小于、等于或大于指定对象的时候，分别返回一个负整数、零或者正整数。如果由于指定对象的类型而无法与该对象进行比较，则抛出ClassCastException异常。

就好像违反了hashCode约定的类会破坏其他依赖于散列做法的类一样，违反compareTo约定的类也会破坏其他依赖于比较关系的类。依赖比较关系的类包括有序集合类TressSet和TreeMap，以及工具类Collections和Arrays，它们内部包含有搜索和排序算法

强烈建议`（x.compareTo(y)==0) ==(x.equals(y))`，但这并非绝对必要。通常，任意实现了Comparable接口的类，若违反了这个条件，都应该明确予以说明。

例如：考虑BigDecimal类，它的compareTo方法与equals不一致。如果你创建了一个HashSet实例，并且添加new BigDecimal("1.0")和new BigDecimal("1.00")，这个集合就将包含两个元素，因为新增到集合中的两个BigDecimal实例，通过equals方法来比较时是不相等的。然而，如果你使用TreeSet而不是HashSet来执行同样的过程，集合中将只包含一个元素，因为这两个BigDecimal实例在通过compareTo方法进行比较时是相等的。

### 三.类和接口

#### 13.使类和成员的可访问性最小化

模块对于外部的其他模块隐藏其内部数据和其他实现细节。设计良好的模块会隐藏所有的实现细节把它的API与它的实现清晰地隔离开来，然后模块之间只通过它们的API进行通信，一个模块不需要知道其他模块内部的工作情况，这个概念被称为信息隐藏和封装。

信息隐藏使得模块可以独立地开发、测试、优化、使用、理解和修改。实体的可访问性是由该实体声明所在的位置，以及该实体声明中所出现的访问修饰符（private/protected/public)共同决定的。

注意：1.如果方法覆盖了超类中的一个方法，子类中的访问级别就不允许低于超类中的访问级别；

2.实例域绝不能是公有的，如果域是非final的或者是一个指向可变对象的final引用，那么一旦使这个域成为公有的，就放弃了对存储在这个域中的值进行限制的能力，这意味着你也放弃了强制这个域不可变的能力

3.长度非零的数组总是可变的，所以，类具有公有的静态final数组域或者返回这种域的访问方法，这几乎总是错误的。如果类具有这样的域或者访问方法，客户端将能够修改数组中的内容，这是安全漏洞的一个常见根源：

~~~java
//potential security hole
public static final Thing[] VALUES = {……};
~~~

许多IDE会产出返回指向私有数组域的引用的访问方法，这样就会产生这个问题。修正这个问题有两种方法。可以使公有数组变成私有的，并增加一个公有的不可变列表：

~~~
private static final Thing[] PRIVATE_VALUES = [……];
public static final List<Thing> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
~~~

另一种方法是，可以使数组变成私有的，并添加一个公有方法，它返回私有数组的一个备份

~~~java
private static final Thing[] PRIVATE_VALUES = {……};
public static final Thing[] values() {
    return PRIVATE_VALUES.clone();
}
~~~

#### 14.在公有类中使用访问方法而非公有域

使用公有的setter、getter方法

#### 15.使可变性最小化

不可变类只是其实例不能被修改的类。每个实例中包含的所有信息都必须在创建该实例的时候就提供，并在对象的整个声明周期内固定不变。String、基本类型的包装类、BigInteger和BigDecimal都是不可变类

为了使类成为不可变，要遵循下面五条规则：1.不要提供任何会修改对象状态的方法(也称为mutator)；2.保证类不会被扩展；3使所有的域都是final的；4.使所有的域都成为私有的；5.确保对于任何可变组件的互斥访问。如果类具有指向可变对象的域，则必须确保该类的客户端无法获得指向这些对象的引用，并且永远不要用客户端提供的对象引用来初始化这样的域，也不要从任何访问方法中返回该对象引用

~~~java
public final class Complex {
    private final double re;
    private final double im;
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }
    //Accessors with no corresponding mutators
    public double realPart() { return re;}
    public double imaginaryPart() {return im;}
    public Complex add(Complex c) {
        return new Complex(re+c.re, im+c.im);
    }
    public Complex subtract(Complex c) {
        return new Complex(re-c.re, im-c.im);
    }
    public Complex multiply(Complex c) {
        return new Complex(re*c.re-im*c.im, re*c.im+im*c.re);
    }
    public Complex divide(Complex c) {
        double tmp = c.re*c.re+c.im*c.im;
        return new Complex ((re*c.re+im*c.im)/tmp,
                           (im*c.re-re*c.im)/tmp);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) { return true;}
        if(!(o instanceof Complex)) {return false;}
        Complex c = (Complex) o;
        return Double.compare(re, c.re)==0 &&
            Double.compare(im, c.im)==0;
    }
    @Override
    public int hashCode() {
        int result = 17+hashDouble(re);
        result=31*result+hashDouble(im);
        return result;
    }
    private int hashDouble(double val) {
        long longBits = Double.doubleToLongBits(re);
        return (int) (longBits ^ (longBits >>> 32));
    }
    @Override
    public String toString() {
        return "(" + re + "+" + im + "i)";
    }
}
~~~

注意这些算术运算是创建并返回新的Complex实例，而不是修改这个实例。大多数重要的不可变类都使用了这种模式，它被称为函数的（functional）做法，因为这些方法会返回一个函数的结果，这些函数对操作数进行运算但并不修改它。与之相对应的更常见的是过程的（procedural）或者命令式的（imperative）做法，使用这些方式时，将一个过程作用在它们的操作数上，会导致它的状态方式改变。

不可变对象是线程安全的，它们不要求同步。不可变对象可以被自由地共享。

不可变的类可以提供一些静态工厂，它们把频繁被请求的实例缓存起来，从而当现有实例可以符合请求的时候，就不必创建新的实例。

不可变类真正唯一的缺点是，对于每个不同的值都需要一个单独的对象。创建这种对象的代价可能很高，特别是对于大型对象的情形。

String类的**可变配套类**StringBuilder（和基本上已经废弃的StringBuffer）

+ 被final修饰的类不可以被继承。 
+ 被final修饰的方法不可以被重写
+ 被final修饰的变量不可变 。可以定义变量时直接赋值，可以在代码块中进行赋值（静态代码块和构造代码块），可以在构造方法中进行赋值，总结起来就是要在对象创建之前完成赋值的过程。对于基本数据类型的，如byte、short、char、int等，赋值后被final修饰的变量不能改变；对于引用数据类型，地址不能改变，但是地址中的值可以发生改变。（String和包装类除外）

#### 16.复合优先于继承

不用扩展现有的类，而是在新的类中增加一个私有域，它引用现有类的一个实例。这种设计被称作“复合”，因为现有的类变成了新类的一个组件。新类中的每个实例方法都可以调用被包含的现有类实例中对应的方法，并返回它的结果。这被称为转发（forwarding），新类中的方法被称作转发方法（forwarding method）

~~~java 
//wrapper class - uses composition in place of inheritance
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;
    public InstrumentedSet(Set<E> s) {
        super(s);
    }
    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }
    @Override
    public boolean addAll(Collection<? entends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }
    public int getAddCount() {
        return addCount;
    }
}

//Reusable forwarding class
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;
    public ForwardingSet(Set<E> s) { this.s = s;}
    public void clead() {s.clear();}
    public boolean contains(Object o) { return s.contains(o);}
    public boolean isEmpty() {return s.isEmpty();}
    public int size() {return s.size();}
    public Iterator<E> iterator() {return s.iterator();}
    public boolean add(E e) {return s.add(e);}
    public boolean remove(Object o) { return s.remove(o);}
    public boolean containsAll(Collect<?> c) {return s.containsAll(c);}
    public boolean addAll (Collection<? extends E> c) {return s.addAll(c);}
    ……;
    @Override
    public boolean equals(Object o) {return s.equals(o);}
    @Override
    public int int hashCode() {return s.hashCode();}
    @Override
    public String toString() {return s.toString();}
}
~~~

因为每一个InstrumentedSet实例都把另一个Set实例包装起来了，所以InstrumentedSet类被称作**包装类（wrapper class）**。这也正是Decorator模式。因为InstrumentedSet类对一个集合进行了修饰，为它增加了计数特性。

只有当子类真正是超类的子类型时，才适合用继承，即“is-a”的关系。即便如此，如果子类和超类处在不同的包中，并且超类并不是为了继承而设计的，那么继承将会导致脆弱性。为了避免这种脆弱性，可以用复合和转发机制来代替继承，尤其是当存在适当的接口可以实现包装类的时候。包装类不仅比子类更加健壮，而且功能也更加强大。

#### 17.要么为继承而设计，并提供文档说明。要么就禁止继承

关于程序文档有句格言：好的API文档应该描述一个给定的方法做了什么工作，而不是描述它是如何做到的。

为了使程序员能够编写出更加有效的子类，而无需承受不必要的痛苦，类必须通过某种形式提供适当的钩子（hook），以便能够进入到它的内部工作流程中，这种形式可以是精心选择的受保护的（protected）方法，也可以是受保护的域，后者比较少见。

#### 18.接口优于抽象类

+ 现有的类可以很容易被更新，以实现新的接口。如果这些方法尚不存在，所需要做的就只是增加一个包含这些方法的接口，然后在类的声明张增加一个implements子句。
+ 接口是定义mixin（混合类型）的理想选择。不严格地讲，mixin是指这样的类型：类除了实现它的“基本类型”之外，还可以实现这个mixin类型，以表明它提供了某些可供选择的行为，例如Comparable就是一个mixin接口。
+ 接口允许我们构造非层次结构的类型框架。

通过包装类（wrapper class）模式（16条），接口使得安全的增强类的功能成为可能。如果使用抽象类来定义类型，那么程序员除了使用继承的手段来增加功能，没有其他选择。

虽然接口不允许包含方法的实现，但是，使用接口来定义类型并不妨碍你为程序员提供实现上的帮助。*通过对你导出的每个重要接口都提供一个抽象的骨架实现（skeletal implementation）类，把接口和抽象类的优点结合起来*。接口的作用仍然是定义类型，但是骨架实现类接管了所有与接口实现相关的工作。

~~~java
//skeletal implementation
public abstract class AbstractMapEntry<K, V> implements Map.Entry {
    //primitive operations
    public abstract K getKey();
    public abstract V getValue();
    //Entries in modifiable maps must override this method
    public V setValue(V value) {
        throw new UnsupportedOperationException()
    }
    //Implements the general contract of Map.Entry.equals
    @Override
    public boolean equals(Object o) {
        if (o==this) {
            return true;
        }
        if(! (o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry<?,?> arg = (Map.Entry) o;
        return equals(getKey(), arg.getKey()) &&
            equals(getValue(), arg.getValue());
    }
    
    private static boolean equals(Object o1, Object o2) {
        return o1==null ? o2==null : o1.equals(o2);
    }
    //Implements the general contrack of Map.Entry.hashCode
    @Override
    public int hashCode() {
        return hashCode(getKey()) ^ hashCode(getValue());
    }
    private static int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }
}
~~~

骨架实现类就是为了继承的目的设计的

使用抽象类来定义允许多个实现的类型，与使用接口相比有一个明显的优势：抽象类的演变比接口的演变要容易的多。如果在后续的发行版本中，你希望在抽象类中增加新的方法，始终可以增加具体方法，它包含合理的默认实现。然后，该抽象类的所有实现都将提供这个新的方法。对于接口，这是行不通的。

简而言之，接口通常是定义允许多个实现的类型的最佳途径。这个规则有个例外，即当演变的容易性比灵活性和功能更为重要的时候。如果你导出了一个重要的接口，就应该坚决考虑同时提供骨架实现类。

#### 19  接口只用于定义类型

有一种接口被称为常量接口，这种接口没有包含任何方法，它只包含静态的final域，每个域都导出一个常量。使用这些常量的类实现这个接口，以避免用类名来修饰常量名。

常量接口模式是对接口的不良使用。类在内部使用某些常量，这纯粹是实现细节。实现常量接口，会导致把这样的实现细节泄露到该类的导出API中。类实现常量接口，这对于这个类的用户来讲并没有什么价值。实际上，这样做反而会使他们更加糊涂。。更糟糕的是，它代表了一种承诺：如果在将来的发行版本中，这个类被修改了，它不再需要使用这些常量，它依然必须实现这个接口，以确保二进制兼容性。如果非final类实现了常量接口，它的所有子类的命名空间也会被接口中的常量“污染”。

可以使用不可实例化的工具类来导出这些常量。

~~~~java
public class PhysicalConstants {
    private PhysicalConstants() {} //prevents instantiation
    public static final double AVOGADROS = 1;
    public static final double COUNT = 2;
}
~~~~

如果大量利用工具类导出的常量，可以通过利用静态导入机制，避免用类名来修饰常量名。

~~~~java
import static com.effectivejava.PhysicalConstants.*;
public class Test {
    double atoms(double count) {
        return COUNT;
    }
}
~~~~

#### 20.类层次优于标签类

#### 21. 用函数对象表示策略

java没有提供函数指针，但是可以用对象引用实现同样的功能。调用对象上的方法通常是执行该对象上的某项操作。然而，**我们也可以定义这样一种对象，它的方法执行其他对象（这些对象被显示传递给这些方法）上的操作**。如果一个类仅仅导出这样的一个方法，它的实例实际上就等同于一个指向该方法的指针。这样的实例被称作函数对象。

函数指针的主要用途就是实现策略模式。为了在java中实现这种模式，要声明一个接口来表示策略，并且为每个具体策略声明一个实现了该接口的类。当一个具体策略只被使用一次时，通常使用匿名类来声明和实例化这个具体策略类。当一个具体策略是设计用来重复使用的时候，它的类通常就要被实现为私有的静态成员类，并通过公有的静态的静态final域被导出，其类型为该策略接口。

~~~java
//exporting a concrete strategy
class Host {
    private static class StrLenCmp implements Comparator<String>, Serialzable {
        public int compare(String s1, String s2) {
            return s1.length() - s2.length();
        }
    }
    
    //return comparator is serializable
    public static final Comparator<String> STRING_LENGTH_COMPARATOR = new StrLenCmp();
    
    …… //Bulk of class omitted
}
~~~

#### 22. 优先考虑静态成员类

嵌套类是指被定义在另一个类的内部的类。嵌套类存在的目的应该知识为它的外围类提供服务。如果嵌套类将来可能会用于其他的某个环境中，它就应该是顶层类。嵌套类有四种：**静态成员类、非静态成员类、匿名类和局部类。**除了第一中之外，其他三种都被称作内部类。

静态成员类是最简单的一种嵌套类。最好把它看做是普通的类，只是碰巧被声明在另一个类的内部而已，它可以访问外围类的所有成员，包括那些声明为私有的成员。静态成员类是外围类的一个静态成员，与其他的静态成员一样，也遵守同样的可访问性规则。如果它被声明私有的，它就只能在外围类的内部才可以被访问。

非静态成员类的一种常见用法是定义一个Adapter，它允许外部类的实例被看做是另一个不相关的类的实例。列如，map接口的实现往往使用非静态成员类来实现他们的*集合视图*，这些集合视图是由Map的keySet、entrySet和Values方法返回的。同样的，诸如Set和List这种集合接口的实现往往也使用非静态成员类来实现它们的迭代器（iterator）

~~~~ java
//typical use of a nonstatic member class
public calss MySet<E> extends AbstractSet<E> {
    ……//bulk of the class
        public Iterator<?> iterator() {
        return new MyIterator();
    }
    
    private class MyIterator implements Iterator<?> {
        ……
    }
}
~~~~

*如果声明成员类不要求访问外围实例，就要始终把static修饰符放在它的声明中，使它成为静态成员类，而不是非静态成员类*。如果省略了static修饰符，则每个实例都将包含一个额外的指向外围对象的引用，消耗时间和空间。

简而言之，如果一个嵌套类需要在单个方法之外仍然是可见的，或者它太长了，不适合放在方法内部，就应该使用成员类。如果成员类的每个实例都需要一个指向其外围实例的引用，就要把成员类做成非静态的；否则，就做成静态的。假设这个嵌套类属于一个方法的内部，如果你只需要在一个地方创建实例，并且已经有了一个预置的类型可以说明这个类的特征，就要把它做成匿名类；否则就做成局部类。

### 四. 泛型

#### 23. 请不要在新代码中使用原生态类型

每种泛型定义一组参数化的类型，构成格式为：先是类或者接口的名称，接着用尖括号(<>)把对应于泛型形式类型参数的实际类型参数列表括起来。例如，List<String>是一个参数化的类型，表示元素类型为String的列表。（String是与形式类型参数E（List<E>)相对应的实际类型参数。

































