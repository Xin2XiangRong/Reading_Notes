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

最后一点，每个泛型都定义一个*原生态类型*，即不带任何实际类型参数的泛型名称。例如，与List<E>相对应的原生态类型是List。原生态类型就像从类型声明中删除了所有泛型信息一样。

如果不提供类型参数，使用集合类型和其他泛型也是合法的，但是不应该这么做。*如果使用原生态类型，就失掉了泛型在安全性和表述性方面的所有优势*。之所以java的设计者还要允许使用原生态类型，是为了提供兼容性。

虽然不应该在新代码中使用像List这样的原生态类型，使用参数化的类型以允许插入任意对象，如List<Object>，这还是可以的。原生态类型List和参数化的类型List<Object>之间到底有什么区别呢？不严格地说，前者逃避了泛型检查，后者则明确告知编译器，它能够持有任意类型的对象。虽然可以将List<String>传递给类型List的参数，但是不能将它传给类型List<Object>的参数。**泛型有子类型化的规则，List<String>是原生态类型List的一个子类型，而不是参数化类型List<Object>的子类型**。因此，如果使用像List这样的原生态类型，就会失掉类型安全性，但是如果使用像List<Object>这样的参数化类型则不会。

这条规则有两个例外：

1. 在类文字中必须使用原生态类型。规范不允许使用参数化类型。换句话说，List.class，String[].class和int.class都合法，但是List<String>.class和List<?>.class则不合法
2. 由于泛型信息可以在运行时被擦除，因此在参数化类型而非无限制通配符类型上使用instanceof操作符是非法的。用无限制通配符类型代替原生态类型，对instanceof操作夫的行为不会产生任何影响。在这种情况下，尖括号(<>)和问号(?)就显得多余了。下面是利用泛型来使用instanceof操作夫的首选方法

~~~java
//Legitimate use of raw type - instanceof operator
if(o instanceof Set) {  //Raw type
    Set<?> m = (Set<?>) o;  //Wildcard type
}
~~~

#### 24. 消除非受检警告

用泛型编程时，会遇到许多编译器警告：非受检强制转化警告（unchecked cast warnings）、非受检方法调用警告、非受检普通数组创建警告，以及非受检转换警告（unchecked conversion warnings）

要尽可能的消除每一个非受检警告。*如果无法消除警告，同时可以证明引起警告的代码是类型安全的，（只有在这种情况下才），可以用一个@SuppressWarning("unchecked")注解来禁止这条警告*。如果在禁止警告之前没有先证实代码类型是安全的，那就只是给自己一种错误的安全感而已，在运行时可能会抛出ClassCastException异常。

SuppressWarnings注解可以用在任何粒度的级别中，从单独的局部变量声明到整个类都可以。*应该始终在尽可能小的范围中使用SuppressWarnings注解*。它通常是个变量声明，或是非常剪短的方法或构造器。如果你发现自己在长度不止一行的方法或者构造器中使用了SuppressWarnings注解，可以将它移到一个局部变量的声明中。虽然你必须声明一个新的变量，不过这么做还是值得的。例如，考虑ArrayList类中的toArray方法：

~~~~java
public <T> T[] toArray(T[] a) {
    if(a.length<size) {
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    }
    if(a.length>size) {
        a[size] = null;
    }
    return a;
}
~~~~

如果编译ArrayList，该方法就会产生这条警告：

~~~java
ArrayList.java:305:warning:[unchecked] uncheckd cast
found : Object[], required: T[]
	return (T[]) Arrays.copyOf(elements, size, a.getClass());
~~~

将SuppressWarnings注解放在return语句中是非法的，因为它不是一个声明。可以声明一个局部变量来保存返回值，并注解其声明

~~~~java
/add local variable to reduce scope of @SuppressWarnings
public <T> T[] toArray(T[] a) {
    if(a.length<size) {
        //this cast is correct because the array we're creating
        //is of the same type as the one passed in, which is T[]
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }
    if(a.length>size) {
        a[size] = null;
    }
    return a;
}
~~~~

总而言之，非受检警告很重要，不要忽略他们。每一条警告都表示可能在运行时抛出ClassCastException异常。要尽最大努力消除这些警告。如果无法消除这些警告，同时可以证明引起警告的代码是类型安全的，就可以在尽可能小的范围中，用@SuppressWarnings("unchecked")注解禁止该警告。要用注解把禁止该警告的原因记录下来。



#### 25、列表优先与数组

数组与泛型相比，有两个重要的不同点。首先，数组是*协变（covariant）*的，即“如果Sub为Super的子类型，那么数组Sub[]就是Super[]的子类型。相反，泛型则是不可变的（invariant）：对于任意两个不同的类型Type1和Type2，List<Type1>既不是List<Type2>的子类型，也不是List<Type2>的超类型。

下面的代码片段是合法的”：

```java
//Fails at Runtime
Object[] objectArray = new Long[1];
objectArray[0] = "i don't fit in"; //throws ArrayStoreException
```

但下面这段代码不合法

```java
//won't compile
List<Object> ol = new ArrayList<Long>();  //incompatible types
ol.add("i don't fit in")
```

这其中无论哪种方法都不能将String放进Long容器中，但是利用数组，只会在运行是发现所犯的错误；利用列表，则可以在编译是发现错误。我们当然希望在编译时发现错误了。

数组与泛型的第二大区别在于，数组是具体化的。因此数组会在运行时才知道并检查它们的元素类型约束。相比之下，泛型则是通过擦除来实现的。因此泛型只在编译时强化它们的类型信息，并在运行是丢弃（或擦除）它们的元素类型信息。擦除就是使泛型可以与没有泛型的代码随意进行互用。———（不可具体化的（non-reifiable）类型是指其运行时表示法包含的信息比它的编译时表示法包含的信息更少的类型，比如像E、List<E>和List<String>这样的类型。

由于上述这些根本的区别，因此数组和泛型不能很好的混合使用。列如，创建泛型、参数化类型或者类型参数的数组是非法的。以下都是非法的：new List<E>[]、new List<String>[]和new E[]。这些在编译是都会导致generic array creation（泛型数组创建）错误。

总而言之，数组和泛型有着非常不同的类型规则。数组是协变且可以具体化的；泛型是不可变的且可以被擦除的。因此，数组提供了运行时的类型安全，但是没有编译时的类型安全，反之，对于泛型也一样。一般来说，数组和泛型不能很好的混合使用。如果你发现自己将它们混合起来使用，并且得到了编译是错误或警告，你的第一反应就应该是用列表代替数组。

#### 26. 优先考虑泛型

总而言之，使用泛型比使用需要在客户端代码中进行转换的类型来得更加安全，也更加容易。在设计新类型的时候，要确保它们不需要这种转换就可以使用。这通常意味这要把类做成是泛型的。只要时间允许，就把现有的类型都泛型化。这对于这些类型的新用户来说会变得更加轻松，又不会破坏现有的客户端。

#### 27. 优先考虑泛型方法

总而言之，泛型方法就像泛型一样，使用起来比要求客户端转换输入参数并返回值的方法来的更加安全，也更加容易。就像类型一样，你应该确保新方法可以不用转换就能使用，这通常意味着要将它们泛型化。并且就像类型一样，还应该将现有的方法泛型化，使新用户使用起来更加轻松，且不会破坏现有的客户端

#### 28. 利用有限制通配符来提升API的灵活性

举例：

```java
//对于stack的pushAll方法，如果如下：
public void pushAll(Iterable<E> src) {
    for(E e:src) push(e);
}

Stack<Number> numberStack = new Stack<Number>();
Iterable<Integer> integers = ...;
numberStack.pushAll(integers);
```

虽然Integer是Number的一个子类型。但是上述代码依旧会得到报错信息，因为参数化类型是不可变的。

解决办法：java提供了一种特殊的参数化类型，称作有限制的通配符类型（bounded wildcard type），来处理类似的情况。pushAll的输入参数类型不应该为“E的Iterable接口”，而应该为“E的某个子类型的Iterable接口”

```java
//修改为如下
public void pushAll(Iterable<? extends E> src) {
    for(E e:src) {
        push(e);
    }
}
```

举例：

```java
public void popAll(Collection<E> dst) {
    while(!isEmpty()) {
        dst.add(pop());
    }
}

Stack<Number> numberStack = new Stack<Number>();
Collection<Object> objects=...;
numberStack.popAll(objects);
```

上面的代码在编译时会得到以下错误：Collection<Object>不是Collection<Number>的子类型。对于此，通配符类型同样提供了一种解决办法。popAll的输入参数类型不应该为“E的集合”，而应该为“E的某种超类的集合。修改如下：

```java
public void popAll(Collection<? super E> dst) {
    while(!isEmpty()) dst.add(pop());
}
```

所以，为了获得最大限度的灵活性，要在表示生产者或者消费这的输入参数上使用通配符类型。如果某个输入参数既是生产者，又是消费者，那么通配符类型对你就没有什么好处了：因为你需要严格的类型匹配。

为了便于记住使用哪种通配符类型：PECS表示prodecer-extends，consumer-super。

换句话说，如果参数化类型表示一个T生产者，就使用<? extends T>；如果它表示一个T消费者，就使用<? super T>.

总而言之，在API中使用通配符类型虽然比较需要技巧，但是使API变得灵活的多。如果编写的是将被广泛使用的类库，则一定要适当地利用通配符类型。

#### 29.优先考虑类型安全的异构容器

泛型最常用于集合，如Set和Map，以及单元素的容器，如ThreadLocal和AtomicReferance。在这些用法中，它都充当被参数化了的容器。这样就限制你每个容器只能有固定数目的类型参数。一般来说，这种情况正是你想要的。一个Set只有一个类型参数，表示它的元素类型；一个Map有两个类型参数，表示它的键和值类型，诸如此类。

但是，有时候你会需要更多的灵活性。例如，数据库可以有任意多的列，如果能以类型安全的方式访问所有列就好了。

集合Api说明了泛型的一般用法，限制你每个容器只能有固定数目的类型参数。你可以通过将类型参数放在键上而不是容器上来避开这一限制。对于这种类型安全的异构容器，可以用Class对象作为键。以这种方式使用的Class对象称作类型令牌。你也可以使用定制的键类型。例如，用一个DatabaseRow类型表示一个数据库行（容器），用泛型Column<T>作为它的键。

### 五. 枚举和注解

#### 30. 用enum代替int常量

在编程语言还没有引入枚举类型之前，表示枚举类型的常用模式是声明一组具名的int常量，每个类型成员一个常量：

```java
//the int enum pattern -- severely deficient
public static final int APPLE_FUJI = 0;
public static final int APPLLE_PIPPIN = 1;

public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
```

这种方法称作*int枚举模式（int enum pattern）*，存在着诸多不足。它在类型安全性和使用方便性方面没有任何帮助。如果你将apple传到想要orange的方法中，编译器也不会出现警告，还会用==操作符将apple与orange进行对比，甚至更糟糕：

```java
//tasty citrus flavored applesause
int i = (ORANGE_TEMPLE - APPLE_FUJI)
```

采用int枚举模式的程序是十分脆弱的。因为int枚举是编译时常量，被编译到使用它们的客户端中。如果与枚举常量关联的int发生了变化，客户端就必须重新编译。如果没有重新编译，程序还是可以运行，但是它们的行为就是不确定的。

而且将int枚举常量翻译成可打印的字符串，并没有很便利的方法。如果将这种常量打印处理，或者从调试器中将它显示出来，你所见到的就是一个数字，这没有太大的用处。要遍历一个组中的所有int枚举常量，甚至获得int枚举组的大小，这些都没有很可靠的方法。

从java1.5发行版本开始，就提出了另一种可以替代的解决方案，可以避免int和string枚举模式的缺点，并提供许多额外的好处：java枚举类型。

java枚举类型背后的基本想法非常简单：*它们就是通过公有的静态final域为每个枚举常量导出实例的类*。因为没有可以访问的构造器，枚举类型是真正的final。因为客户端既不能创建枚举类型的实例，也不能对它进行扩展，因此很可能没有实例，而只有声明过的枚举常量。换句话说，枚举类型是实例受控的。它们是单列的泛型化，本质上是单元素的枚举。

枚举类型的例子：

```java
//Enum type with data and behavior
public enum Planet {
    MERCURY(3.302E+23, 2.439E6),
    VENUS(4.869E+24, 6.054E5),
    EARTCH(5.975E+24, 6.389E3),
    NEPTUNE(1.034E+26, 2.4777E7);
    private final double mass;     //in kilograms
    private final double radius;     //in meters
    private final double surfaceGravity;    //in m/s^2
    
    //Universal gravitational constant in m^3/kg s^2
    private static final double G = 6.67300E-11;
    
    //Constructor
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass/(radius*radius);
    }
    
    public double mass() {return mass;}
    public double radius() {return radius;}
    public double surfaceGravity() {return surfaceGravity;}
    
    public double surfaceWeight(double mass) {
        return mass * surfaceGravity; // F = ma
    }
}
```

编写一个像Planet这样的枚举类型并不难。为了**将数据与枚举常量关联起来，得声明实例域，并编写一个带有数据并将数据保存在域中的构造器**。枚举天生就是不可变的，因此所有的域都应该为final的。它们可以是公有的，但最好将它们做成私有的，并提供公有的访问方法。

所有的枚举都有一个静态的*values方法*，按照声明顺序返回它的值数组。

假设你在编写一个枚举类型，来表示计算器的四大基本操作（即加减乘除），你想要提供一个方法来执行每个常量所表示的算术运算。有一种方法是通过启用枚举的值来实现的：

```java
//enum type that switches on its own value--questionable
public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;
    //Do the arithmetic op represented by this constant
    double apply(double x, double y) {
        switch(this) {
            case PLUS: return x+y;
            case MINUS: return x-y;
            case TIMES: return x*y;
            case DIVIDES: return x/y;
        }
        throw new AssertionError("Unknown op: " + this);
    }
}
```

这段代码如果没有throw语句，它就不能进行编译，虽然从技术角度来看代码的结束部分是可以执行到的，但是实际上是不可能执行到这行代码的。而且这段代码很脆弱，如果你添加了新的枚举常量，却忘记给switch添加相应的条件，枚举仍然可以编译，但是当你试图运用新的运算是，就会运行失败。

更好的方法是可以将不同的行为与每个枚举常量关联起来：*在枚举类型中声明一个抽象的apply方法，并在特定于常量的类主体（constant-specific class body)中，用具体的方法覆盖每个常量的抽象apply方法*。这种方法被称作特定于常量的方法实现

```java
//enum type with constant-specific method implementations
public enum Operation {
    PLUS {double apply(double x, double y) {return x+y;}},
    MINUS {double apply(double x, double y) {return x-y;}},
    TIMES {double apply(double x, double y) {return x*y;}},
    DIVIDE {double apply(double x, double y) {return x/y;}};
    
    abstract double apply(double x, double y);
}
```

如果给Operation的第二种版本添加新的常量，你就不可能会忘记提供apply方法，因为该方法就紧跟在每个常量声明之后。而且编译器也会提醒你，因为枚举类型中的抽象方法必须被它所有常量中的具体方法所覆盖。

特定于常量的方法实现可以与特定于常量的数据结合起来。例如，下面的Operation覆盖了toString来返回通常与该操作关联的符号：

```java
public enum Operation {
    PLUS("+") {double apply(double x, double y) {return x+y;}},
    MINUS("-") {double apply(double x, double y) {return x-y;}},
    TIMES("*") {double apply(double x, double y) {return x*y;}},
    DIVIDE("/") {double apply(double x, double y) {return x/y;}};
    
    private final String symbol;
    Operation(String symbol) {this.symbol = symbol;}
    @Override
    public String toString() {
        return symbol;
    }
    
    abstract double apply(double x, double y);
}
```

枚举类型有一个自动产生的valuesOf(String)方法，它将常量的名字转变成常量本身。如果在枚举类型中覆盖toString，要考虑编写一个fromString方法，将定制的字符串表示法变回响应的枚举。下列代码（适当地改变了类型名称）可以为任何枚举完成这样技巧，只要每个常量都有一个独特的字符串表示法：

```java
//implementing a fromString method on an enum type
private static final Map<String, Operation> stringToEnum 
= new HashMap<String, Operation>();
static {
    for (Operation op : values()) {
        stringToEnum.put(op.toString(), op);
    }
}
//return Operation for string, or null if string is invalid
public static Operation fromString(String symbol) {
    return stringToEnum.get(symbol);
}
```

当你真正想要的就是每当添加一个枚举常量时，就强制选择一种加班报酬策略。这种想法就是将加班工资计算移到一个私有的嵌套枚举中，将这个*策略枚举（strategy enum)*的实例传到PayrollDay枚举的构造器中。之后PayrollDay枚举将加班工资计算委托给策略枚举，PayrollDay中就不需要switch语句或者特定于常量的方法实现了。虽然这种模式没有switch语句那么简洁，但更加安全，也更加灵活：

```java
//the strategy enum pattern
enum PayrollDay {
    MONDAY(PayType.WEEKDAY), TUESDAY(PayType.WEEKDAY), WEDNESDAY(PayType.WEEKDAY), THURSDAY(PayType.WEEKDAY), FRIDAY(PayType.WEEKDAY), SATURDAY(PayType.WEEKEND), SUNDY(PayType.WEENEND);
    
    private final PayType payType;
    PayrollDay(PayType payType) {this.payType = payType;}
    
    double pay(double hoursWorked, double payRate) {
        return payType.pay(hoursWorked, payRate);
    }
    
    //the strategy enum type
    private enum PayType {
        WEEKDAY {
            double overtimePay(double hours, double payRate) {
                return hours <= HOURS_PER_SHIFT ? 0 :(hours - HOURS_PER_SHIFT)*payRate/2;
            }
        },
        WEEKEND {
            double overtimePay(double hours, double payRate) {
                return hours *payRate/2;
            }
        };
        private static final int HOURS_PER_SHIFT = 8;
        abstract double overtimePay(double hrs, double payRate);
        double pay(double hoursWorked, double payRate) {
            double basePay = hoursWorded*payRate;
            return basePay + overtimePay(hoursWorked, payRate);
        }
    }
}
```

总而言之，与int常量相比，枚举类型的优势是不言而喻的。枚举要易懂的多，也更加安全，功能更加强大。许多枚举都不需要显式的构造器或者成员，但许多其他枚举则收益于“每个常量与属性的关联”以及“提供行为受这个属性影响的方法”。只有极少数的枚举受益于将多种行为与单个方法关联。在这种相对少见的情况下，特定于常量的方法要优先于启用自有值的枚举。如果多个枚举常量同时共享相同的行为，则考虑策略枚举。

#### 31. 用实例域代替序数

许多枚举天生就与一个单独的int值相关联。所有的枚举都有一个ordinary方法，它返回每个枚举常量在类型中的数字位置。你可以试着从序数中得到关联的int值：

```java
//abuse of ordinal to derive an associated value--don't do this
public enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET, SEXTET, SEPTET, OCTET, NONET, DECTET;
    public int numberOfMusicians() {
        return ordinal() + 1;
    }
}
```

虽然这个枚举不错，但是维护起来就像一场恶梦。如果常量进行重新排序，numberOfMusicians方法就会遭到破坏。如果要再添加一个与已经用过的int值关联的枚举常量，就没那么走运了。例如，给双四重奏（double quartet）添加一个常量，它就像个八重奏一样，是有8位演奏家组成的，但是没有办法做到。

幸运的是，有一种简单的方法可以解决这些问题。*永远不要根据枚举的序数导出与它关联的值，而是要将它保存在一个实例域中*：

```java
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5);
    private final int numberOfMusicians;
    Ensemble(int size){
        this.numberOfMusicians = size;
    }
    public int numberOfMusicians() {
        return numberOfMusicians;
    }
}
```

Enum规范中谈到ordinal时这么写到：“大多数程序员都不需要这个方法。它是设计成用于像EnumSet和EnumMap这种基于枚举的通用数据结构的。”除非你在编写的是这种数据结构，否则最好完全避免使用ordinal方法。

#### 32.用EnumSet代替位域？

？？？

#### 33. 用EnumMap代替序数索引

有时候，可能会见到用ordinal方法来索引数组的代码。例如下面这个类用来表示一种烹饪用的香草：

```java
public class Herb {
    public enum Type {
        ANNUAL, PERENNIAL, BIENNIAL
    }
    private final String name;
    private final Type type;
    Herb(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    @Override
    public String toString() {
        return name;
    }
}
```

假设现在有一个香草的数组，表示一座花园中的植物，你想要按照类型（一年生、多年生或者两年生植物）进行组织之后将这些植物列出来。有些程序员会将这些集合放到一个按照类型的序数进行索引的数组中来实现这一点。

```java
//using ordinal() to index an array--don't do this
Herb[] garden = ...;
Set<Herb> herbsByType = (Set<Herb>[]) new Set[Herb.Type.values().length];  //indexed by Herb.Type.ordinal()
for(int i=0;i<herbsByType.length;i++) {
    herbsByType[i]=new HashSet<Herb>();
}
for(Herb h:garden) {
    herbsByType[h.type.ordinal()].add(h);
}

```

这种方法可行，但是隐藏着许多问题：因为数组不能与泛型兼容，程序需要进行未受检的转换，并且不能正确无误地进行编译。因为数组不知道它的索引代表着什么，你必须手工标注（label）这些索引的输出。但是这种方法最严重的问题在于，当你访问一个按照枚举的序数进行索引的数组时，使用正确的int值就是你的职责了；int不能提供枚举的类型安全。你如果使用了错误的值，程序就会悄悄地完成错误的工作，或者幸运的话，会抛出ArryayIndexOutBoundException异常。

用EnumMap改写后的程序：

```java
//Using an EnumMap to associate data with an enum
Map<Herb.Type, Set<Herb>> herbsByType=new EnumMap<Herb.Type, Set<Herb>>(Herb.Type.class);
for(Herb.Type t : Herb.Type.values()) {
    herbsByType.put(t, new HashSet<Herb>());
}
for(Herb h: garden) {
    herbsByType.get(h.type).add(h);
}
```

注意EnumMap构造器采用键类型的class对象：这是一个有限制的类型令牌（bounded type token），它提供了运行时的泛型信息。

```java
//using a nested EnumMap to associate data with enum paris
public enum Phase{
    SOLID, LIQUID, GAS;
    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);
        
        private final Phase src;
        private final Phase dst;
        Transition(Phase src, Phase dst) {
            this.src = src;
            this.dst = dst;
        }
        //initialize the phase transition map
        private static final Map<Phase, Map<Phase,Thransition>> m = new EnumMap<Phase,Map<Phase, Thransition>>(Phase.class);
        static {
            for(Phase p : Phase.values()) {
                m.put(p, new EnumMap<Phase, Thransition>(Phase.class));
            }
            for(Transition trans : Transition.values()) {
                m.get(trans.src).put(trans.dst, trans);
            }
        }
        public static Transition from(Phase src, Phase dst) {
            return m.get(src).get(dst);
        }
    }
}
```

总而言之，*最好不要用序数来索引数组，而要使用EnumMap。*如果你所表示的这种关系是多维的，就使用EnumMap<..., EnumMap<...>>。应用程序的程序员在一般情况下都不使用Enum.ordinal，即使要用也很少，因此这是一种特殊情况。

#### 34. 用接口模拟可伸缩的枚举

举例：

```java
//emulated extensible enum using an interface
public interface Operation {
    double apply(double x, double y);
}
public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x+y;}
    },
    MINUS("-") {
        public double apply(double x, double y) { return x-y;}
    },
    TIMES("*") {
        public double apply(double x, double y) { return x*y;}
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x/y;}
    };
    private final String symbol;
    BasicOperation(String symbol) {
        this.symbol = symbol;
    }
    @Override
    public String toString() {
        return symbol;
    }
}
```

虽然枚举类型（BasicOperation）不是可扩展的，但接口类型（Operation）则是可扩展的，它是用来表示API中的操作的接口类型。假设你想要定义一个上述操作类型的扩展：

```java
//emulated extension enum
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) { return Math.pow(x,y);}
    },
    REMAINDER("%") {
        public double apply(double x, double y) { return x%y;}
    };
    private final String symbol;
    BasicOperation(String symbol) {
        this.symbol = symbol;
    }
    @Override
    public String toString() {
        return symbol;
    }
}
```

通过下面的测试程序体验一些上面定义过的所有扩展过的操作：

```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(ExtendedOperation.class, x, y);
}
private static <T extends Enum<T> & Operation> void test (Class<T> opSet, double x, double y) {
    for(Operation op:opSet.getEnumConstants()) {
        System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x,y));
    }
}
```

第二种方法是使用Collection<? Extends Operation>，这是个有限制的通配符类型，作为opSet参数的类型：

```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(Arrays.asList(ExtendedOperation.values()), x,y);
}
private static void test(Collection<? extends Operation> opSet, double x, doublezz)

```

总而言之，*虽然无法编写可扩展的枚举类型，却可以通过编写接口以及实现该接口的基础枚举类型，对它进行模拟*。这样允许客户端编写自己的枚举来实现接口。如果API是根据接口编写的，那么在可以使用基础枚举类型的任何地方，也都可以使用这些枚举。

#### 35. 注解优先于命名模式

#### 36. 坚持使用Override注解

#### 37. 用标记接口定义类型

标记接口（marker interface）是没有包含方法声明的接口，而只是指明（或者“标明”）一个类实现了具有某种属性的接口。例如，考虑Serializable接口。通过实现这个接口，类表明它的实例可以被写到ObjectOutputStream（或者“被序列化”）。

总而言之，标记接口和标记注解都各有用处。如果想要定义一个任何新方法都不会与之关联的类型，标记接口就是最好的选择。如果想要标记程序元素而非类和接口，考虑到未来可能要给标记添加更多的信息，或者标记要适合于已经广泛使用了注解类型的框架，那么标记注解就是正确的选择。*如果你发现自己在编写的是目标为ElementType.TYPE的标记注解类型，就要花点时间考虑清楚，它是否真的应该为注解类型，想想标注接口是否会更加合适呢。*

### 六. 方法

#### 38. 检查参数的有效性

绝大多数方法和构造器对于传递给它们的参数值都会有某些限制。例如，索引值必须是非负数的，对象引用不能为null，等等，这些都是很常见的。

对于公有的方法，要用Javadoc的@throws标签（tag）在文档中说明违反参数限制时会抛出的异常。这样的异常通常为IllegelArgumentException、IndexOutOfBoundsException或NullPointerException。

对于未被导出的方法（unexpected method），作为包的创建者，你可以控制这个方法将在哪些情况下被调用，因此你可以，也应该确保只将有效的参数值传递进来。因此，非公有的方法通常应该使用断言（assertion）来检查它们的参数，具体做法如下所示：

```java
private static void sort(long a[], int offset, int length) {
    assert a != null;
    assert offset>=0 && offset <= a.length;
    assert length >= 0 && length <= a.length-offset;
    ....// do the computation
}
```

·不同于一般的有效性检查，断言如果失败，将会抛出AssertionError。也不同于一般的有效性检查，如果它们没起到作用，本质上也不会有成本开销，除非通过将-ea（或者-enableassertions）标记（flag）传递给java解释器，来启用它们。

在方法执行它的计算任务之前，应该先检查它的参数，这一规则也有例外。一个很重要的例外是，在有些情况下，有效性检查工作非常昂贵，或者根本是不切实际的，而且有效性检查已隐含在计算过程中完成。例如Collections.sort(List)方法，如果这个对象不能相互比较，其中的某个比较操作就会抛出ClassCastException，这正是sort方法所应该做的事情，因此，提前检查列表中的元素是否可以相互比较并没有多大意义。

并不是对参数的任何限制都是件好事。相反，在设计方法时，应该使它们尽可能地通用，并符合实际的需要。假如方法对于它能接受的所有参数值都能够完成合理的工作，对参数的限制就应该是越少越好。

#### 39. 必要时进行保护性拷贝

即使在安全的语言中，如果不采取一点措施，还是无法与其他的类隔离开来。*假设类的客户端会尽其所能来破坏这个类的约束条件，因此你必须保护性的设计程序。*没有对象的帮助时，虽然另一个类不可能修改对象的内部状态，但是对象很容易在无意识的情况下提供这种帮助。例如考虑下这段代码是否真的提供了一段不可变的时间周期：

```java
//broken "immutable" time period class
public final class Period {
    private final Date start;
    private final Date end;
    /**
     *@param start the begining of the period
     *@param end the end of the period; must not percede start
     *@throws IllegalArgumentException if start is after end
     *@throws NullPointerException if start or end is null
     */
    public Period(Date start, Date end) {
       if(start.compareTo(end) > 0) {
           throw new IllArgumentException(start + " after " +　end);
       } 
        this.start = start;
        this.end = end;
    }
    public Date start() {
        return start;
    }
    public Date end() {
        return end;
    }
    ...
}
```

乍一看，这个类似乎是不可变的。然而，因为Date类本身是可变的，因此很容易违反这个约束条件：

```java
//attack the internals of a period instance
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78);    //Modifies internals of p!
```

为了保护Period实例的内部信息免受到这种攻击，*对于构造器的每个可变参数进行保护性拷贝是必要的*，并且使用备份对象作为Period实例的组件，而不使用原始的对象：

```java
//required constructor - makes defensive copies of parameters
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
    if(this.start.compareTo(this.end) > 0){
        throw new IllArgumentException(start + " after " +　end);
    }
}
```

用了新的构造器之后，上述的攻击对于Period实例不再有效。但是改变Period实例仍然是有可能的：

```java
//second attack on the internals of a Period instance
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78);
```

为了防御这第二种攻击，只需要修改这两个方法，使它返回可变内部域的保护性拷贝即可：

```java
public Date start() {
    return new Date(start.getTime());
}
public Date end() {
    return new Date(end.getTime());
}
```

参数的保护性拷贝并不仅仅针对不可变类。每当编写方法或者构造器时，如果它要允许客户提供的对象进入到内部数据结构中，则有必要考虑一些，客户提供的对象是否有可能是可变的。如果是，就要考虑你的类是否能够容忍对象进入数据结构之后发生变化。如果答案是否定的，就必须进行保护性拷贝，并且让拷贝之后的对象而不是原始对象进入到数据结构中。例如，如果你正在考虑使用由客户提供的对象引用作为内部set实例的元素，或者作为内部map实例的键（key），就应该意识到，如果这个对象在插入之后在被修改，set或者Map的约束条件就会遭到破坏。

在前面Period例子中，值得一提的是，有经验的程序员通常使用Date.getTime()返回的long基本类型作为内部的时间表示法，而不是使用Date对象引用。他们之所以这样做，主要是因为Date是可变的。

如果类所包含的方法或者构造器的调用需要移交对象的控制权，这个类就无法让自身抵御恶意的客户端。只有当类和它的客户端之间有着互相的信任，或者破坏类的约束条件不会伤害到除了客户端之外的其他对象时，这种类才是可以接受的。后一种情形的例子是包装类模式（wrapper class pattern）。根据包装类的本质特征，客户端只需在对象被包装之后直接访问它，就可以破坏包装类的约束条件，但是，这么做往往只会伤害到客户端自己。

简而言之，如果类具有从客户端得到或者返回客户端的可变组件，类就必须保护性的拷贝这些组件。如果拷贝的成本受到限制，并且类信任它的客户端不会不恰当地修改组件，就可以在文档中指明客户端的职责是不得修改受到影响的组件，以此来代替保护性拷贝。

#### 40. 谨慎设计方法签名

* 谨慎的选择方法的名称
* 不要过于追求提供便利的方法。每个方法都应该尽其所能
* 避免过长的参数列表。目标是四个参数，或者更少。

对于参数类型，要优先使用接口而不是类。只要有适当的接口可用来定义参数，就优先使用这个接口，而不是使用实现该接口的类。例如，没有理由在编写方法时使用HashMap类来作为输入，相反，应当使用Map接口作为参数。

#### 41. 慎用重载

简而言之，“能够重载方法”并不意味着就“应该重载方法”。一般情况下，对于多个具有相同参数数目的方法来说，应该尽量避免重载方法。在某些情况下，特别是涉及构造器的时候，要遵循这条建议也许是不可能的。在这种情况下，至少应该避免这样的情形：同一组参数只需经过类型转换就可以被传递给不同的重载方法。如果不能避免这种情形，例如，因为正在改造一个现有的类以实现新的接口，就应该保证：当传递同样参数时，所有重载方法的行为必须一致。如果不能做到这一点，程序员就很难有效地使用被重载的方法或者构造器，他们就不能理解它为什么不能正常工作。

#### 42. 慎用可变参数

简而言之，在定义参数数目不定的方法时，可变参数方法是一种很方便的方式，但是它们不应该被过度滥用。如果使用不当，会产生混乱的结果。

#### 43. 返回零长度的数组或者集合，而不是null

对于一个返回null而不是零长度数组或者集合的方法，几乎每次在使用的时候都需要作是否为null的处理不合常理。

#### 44.为所有导出的API元素编写文档注释

### 七.通用程序设计

#### 45.将局部变量的作用域最小化

要使局部变量的作用域最小化，最有力的方法就是在第一次使用它的地方声明。

几乎每个局部变量的声明都应该包含一个初始化表达式。如果还没有足够的信息来对一个变量进行有意义的初始化，就应该推迟这个声明，直到可以初始化为止。

另一种“将局部变量的作用域最小化”的方法是使方法小而集中。如果把两个操作合并到同一个方法中，与其中一个操作相关的局部变量就有可能会出现在执行另一个操作的代码范围之内。为了防止这种情况发生，只要把这个方法分成两个，每个方法各执行一个操作。

#### 46.for-each循环优先于传统的for循环

for-each循环在简洁性和预防Bug方面有着传统的for循环无法比拟的优势，并且没有性能损失。应该尽可能地使用for-each循环。遗憾的是，有三种常见的情况无法使用for-each循环：

+ 过滤——如果需要遍历集合，并删除选定的元素，就需要使用显式的迭代器，以便可以调用它的remove方法
+ 转换——如果需要遍历列表或者数组，并取代它部分或者全部的元素值，就需要列表迭代器或者数组索引，以便设定元素的值。
+ 平行迭代——如果需要并行地遍历多个集合，就需要显式地控制迭代器或者索引变量，以便所有迭代器或者索引变量都可以得到同步前移。

#### 47.了解和使用类库

#### 48.如果需要精确的答案，请避免使用float和double

float和double类型主要是为了科学计算和工程计算而设计的。它们执行二进制浮点运算，这是为了在广泛的数值范围上提供较为准确的快速近似计算而精心设计的。然而它们并没有提供完全精确的结果，所以不应该被用于需要精确结果的场合。*float和double类型尤其不适合于货币计算*，因为要让一个float或者double精确地表示0.1（或者10的任何其他负数次方值）是不可能的。

如果你想让系统来记录十进制小数点，并且不介意因为不使用基本类型而带来的不便，就请使用BigDecimal。使用BigDecimal还有一些额外的好处，它允许你完全控制舍入，每当一个操作涉及舍入的时候，它允许你从8种舍入模式中选择其一。如果你正通过法定要求的舍入行为进行业务计算，使用BigDecimal是非常方便的。如果性能非常关键，并且你又不介意自己记录十进制小数点，而且所涉及的数值又不太大，就可以使用int或者long。如果数值范围没有超过9位十进制数字，就可以使用int；不超过18位数字，就可以使用long。如果可能超过18位数字，就必须使用BigDecimal。

#### 49.基本类型优先与装箱基本类型

java的类型系统由两部分组成：基本类型和引用类型。

```java
public class Unbelievable {
    static Integer i;
    public static void main(String[] args) {
        if(i == 42) {
            System.out.println("Unbelievable");
        }
    }
}
```

这段代码并不会打印出Unbelievable，它在计算表达式(i==42)的时候抛出NullPinterException异常。问题在于，i是个Integer，而不是int，就像所有的对象引用域一样，它的初始值为null。当程序计算表达式（i==42)时，它会将Integer与int进行比较。*当在一项操作中混合使用基本类型和装箱基本类型时，装箱基本类型就会自动拆箱*。如果null对象引用被自动拆箱，就会得到一个NullPointerException异常。修正这个问题只需要把i声明为int类型。

```java
public static void main(String[] args) {
    Long sum = 0L;
    for(long i=0;i<Integer.MAX_VALUE;i++) {
        sum += i;
    }
    System.out.println(sum);
}
```

这段代码运行起来比预计的要慢一些，因为sum被声明为是装箱基本类型Long，程序编译起来没有错误或者警告，变量被反复的装箱和拆箱，导致明显的性能下降。

什么时候应该使用装箱基本类型呢？它们有几个合理的用处。第一个是作为集合中的元素、键和值。你不能将基本类型放在集合中，因此必须使用装箱基本类型。在参数化类型中，必须使用装箱基本类型作为类型参数，因为java不允许使用基本类型。例如，不能将变量声明为ThreadLocal<int>类型，因此必须使用ThreadLocal<Integer>代替。最后，在进行反射的方法调用时，必须使用装箱基本类型。

总之，当可以选择的时候，基本类型要优先于装箱基本类型。基本类型更加简单，也更加快速。如果必须使用装箱基本类型，要特别小心。

#### 50.如果其他类型更适合，则尽量避免使用字符串

字符串不适合代替其他的值类型；字符串不适合代替枚举类型；字符串不适合代替聚焦类型（如果一个实体有多个组件，用一个字符串来表示这个实体通常是很不恰当的）；字符串也不适合代替能力集

总而言之，如果可以使用更加合适的数据类型，或者可以编写更加适当的数据类型，就应该避免用字符串来表示对象。若使用不当，字符串会比其他的类型更加笨拙、更不灵活、速度更慢，也更容易出错。经常被错误的用字符串来代替的类型包括基本类型。枚举类型和聚集类型。

#### 51.当心字符串连接的性能

原则很简单：不要使用字符串连接操作符来合并多个字符串，除非性能无关紧要。相反，应该使用StringBuilder的append方法。另一种方法是，使用字符数组，或者每次只处理一个字符串，而不是将它们组合起来。

#### 52.通过接口引用对象

应该优先使用接口而不是类来引用对象。*如果有合适的接口类型存在，那么对于参数、返回值、变量和域来说，就都应该使用接口类型进行声明*。只有当你利用构造器创建某个对象的时候，才真正需要引用这个对象的类。例如

```java
//Good--uses interface as type
List<Subscribe> subscribes = new Vector<Subscribe>();
//bad--uses class as type
Vector<Subscribe> subscribers = new Vector<Subsrcribe>()
```

在用接口作为类型，当你决定更换实现时，所要做的就只是改变构造器中类的名称（或者使用一个不同的静态工厂）。周围的代码并不知道原来的实现类型，可以继续工作，它们对于这种变化并不在意。

*如果没有合适的接口存在，完全可以使用类而不是接口来引用对象*

不存在适当接口类型的另一种情形是，类实现了接口，但是它提供了接口中不存在的额外方法。如果程序依赖于这些额外的方法，这种类就应该只被用来引用它的实例。

实际上，给定的对象是否具有适当的接口应该是很显然的。如果是，用接口引用对象就会使程序更加灵活；如果不是，则使用类层次结构中提供了必要功能的最基础的类。

#### 53.接口优先于反射机制

核心反射机制java.lang.reflect，提供了“通过程序来访问关于已装载的类的信息”的能力。反射机制允许一个类使用另一个类，即使当前者被编译的时候后者根本不存在。然而这种能力也要付出代价：

* 丧失了编译时类型检查的好处
* 执行反射访问所需要的代码非常笨拙和冗长
* 性能损失

简而言之，反射机制是一种功能强大的机制，对于特定的复杂系统编程任务，它是非常必要的，但它也有一些缺点。如果你编写的程序必须要与编译时未知的类一起工作，如有可能，就应该仅仅使用反射机制来实例化对象，而访问对象时则使用编译时已知的某个接口或者超类。

#### 54.谨慎地使用本地方法

Java Native Interface（JNI）允许java应用程序可以调用本地方法，本地方法在本地语言中可以执行任意的计算任务，并返回到java程序设计语言。

因为本地语言是与平台相关的，使用本地方法的应用程序也不再是可自由移植的。使用本地方法的应用程序也更难调试。在进入和退出本地代码时，需要相关的固定开销，所以，如果本地代码只是做少量的工作，本地方法就可能降低性能。最后一点，需要“胶合代码”的本地方法编写起来单调乏味，并且难以阅读。

总而言之，在使用本地方法之前务必三思。极少数情况下会需要使用本地方法来提高性能。如果你必须要使用本地方法来访问底层的资源，或者遗留代码库，也要尽可能少用本地代码，并且要全面进行测试。本地代码中的一个bug就有可能破坏整个应用程序。

#### 55.谨慎地进行优化

#### 56.遵守普遍接受的命名惯例

### 八. 异常

#### 57. 只针对异常的情况才使用异常

```java
//horrible abuse of exception. Don't ever do this
try {
    int i=0;
    while(true) {
        range[i++].climb();
    }
} catch(ArrayIndexOutOfBoundsException e){
    //do something
}
```

异常机制的设计初衷是用于不正常的情形，所以很少会有jvm实现试图对它们进行优化，使得与显式的测试一样快速；把代码放在try-catch块中反而阻止了现代jvm实现本来可能要执行的某些特定优化。

总而言之，异常是为了在异常情况下使用而设计的。不要将它们用于普遍的控制流，也不要编写迫使它们这么做的API.

#### 58. 对可恢复的情况使用受检异常，对编程错误使用运行时异常

#### 59. 避免不必要地使用受检的异常？？？

#### 60.优先使用标准的异常

常用的异常如下：

| 异常                            | 使用场合                                     |
| ------------------------------- | -------------------------------------------- |
| IllegalArgumentException        | 非null的参数值不正确                         |
| IllegelStateException           | 对于方法调用而言，对象状态不合适             |
| NullPointerException            | 在禁止使用null的情况下参数值为null           |
| IndexOutOfBoundsException       | 下标参数值越界                               |
| ConcurrentModificationException | 在禁止并发修改的情况下，检测到对象的并发修改 |
| UnsupportedOperationException   | 对象不支持用户请求的方法                     |

#### 61.抛出与抽象相对应的异常

如果方法抛出的异常与它所执行的任务没有明显的联系，这种情形将会使人不知所措。当方法传递由底层抽象抛出的异常时，往往会发生这种情况。除了使人感到困惑之外，这也让实现细节污染了更高层的API。如果高层的实现在后续的发行版本中发生了变化，它所抛出的异常也可能会跟着发生变化，从而潜在的破坏现有的客户端程序。

为了避免这个问题，*更高层的实现应该捕获底层的异常，同时抛出可以按照高层抽象进行解释的异常。*这种做法被称作异常转译（Exception transaction）

```java
//exception transaction
try {
    //use lower-level abstraction to do our bidding
    ...
} catch(LowerLeverlException e) {
    throw new HigherLevelException(...);
}
```

一种特殊的异常转译形式称为异常链（Exception chaining），如果底层的异常对于调试导致高层异常的问题非常有帮助，使用异常链就很合适。底层的异常（原因）被传到高层的异常，高层的异常提供访问方法（Throwable.getCause）来获得底层的异常。

```java
//exception chaining
try{
    ...  //use lower-level abstraction to do our bidding
} catch(LowerLevelException cause) {
    throw new HigherLevelException(cause);
}
```

总而言之，如果不能阻止或者处理来自更底层的异常，一般的做法是使用异常转译，除非底层方法碰巧可以保证它抛出的所有异常对高层也合适才可以将异常从底层传播到高层。异常链对高层和底层异常都提供了最佳的功能：它允许抛出适当的高层异常，同时又能捕获底层的原因进行失败分析。

#### 62. 每个方法抛出的异常都要有文档

#### 63. 在细节消息中包含能捕获失败的信息

当程序由于未被捕获的异常而失败的时候，系统会自动的打印出该异常的堆栈轨迹。在堆栈轨迹中包含该异常的字符串表示法，即它的toString方法的调用结果。它通常包含该异常的类名，紧随其后的是细节消息（detail message）。

*为了捕获失败，异常的细节信息应该包含所有的“对该异常有贡献”的参数和域的值*。例如，IndexOutOfBoundsException异常的细节消息应该包含下界、上界以及没有落在界内的下标值。

为了确保在异常的细节消息中包含足够的能捕获失败的信息，一种办法是在异常的构造器而不是字符串细节消息中引入这些消息。然后，有了这些消息，只要把它们放到消息描述中，就可以自动产生细节消息。例如，IndexOutBoundsException并不是有个String构造器，而是有个这样的构造器：

```java
public IndexOutBoudsException（int lowerBound, int upperBound, int index) {
    super("Lower bound: " + lowerBound + ", Upper bound: " + upperBound + 
         ", Index: " + index);
    //save failure information for programmatic access
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.index = index;
}
```

#### 64. 努力使失败保持原子性

当对象抛出异常之后，通常我们期望这个对象仍然保持在一种定义良好的可用状态之中，即使失败是发生在执行某个操作的过程中间。*一般而言，失败的方法调用应该使对象保持在被调用之前的状态*。具有这种属性的方法被称作具有失败原子性。

1.最简单的办法莫过于设计一个不可变的对象。

2.对于在可变对象上执行操作的办法，获得失败原子性最常见的办法是，在执行操作之前检查参数的有效性。这可以使得在对象的状态被修改之前，先抛出适当的异常。

```java
public Object pop() {
    if(size==0) {
        throw new EmptyStackException();
    }
    Object result = elements[--size];
    elements[size] = null;
    return result;
}
```

如果取消对初始大小（size）的检查，当这个方法企图从一个空栈中弹出元素时，它仍然会抛出异常。然而，这将会导致size域保持在不一致的状态（负数）之中，从而导致将来对该对象的任何方法调用都会失败。

3.调整计算处理过程的顺序，使得任何可能会失败的计算部分都在对象状态被修改之前发生。例如向treeMap中添加元素。

4.编写一段恢复代码，由它来拦截操作过程中发生的失败，以及使对象回滚到操作开始之前的状态上。这种办法主要用于永久性的（基于磁盘的（disk-based））数据结构。

5.在对象的一份临时拷贝上执行操作，当操作完成之后再用临时拷贝中的结果代替对象的内容。例如Collections.sort.

#### 65.不要忽略异常

```java
//empty catch block ignores exception
try {
    ...
} catch(SomeException e) {

}
```

空的catch块会使异常达不到应有的目的，即强迫你处理异常的情况。至少，catch块也应该包含一条说明，解释为什么可以忽略这个异常。

### 九. 并发

#### 66. 同步访问共享的可变数据

```java
//broken! how long would you expect this program to run?
public class StopThread {
    private static boolean stopRequested;
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(new Runnable() {
            public void run() {
                int i = 0;
                while(!stopRequest) {
                    i++;
                }
            }
        });
        backgroundThread.start();
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```

这段代码的问题在于：由于没有同步，就不能保证后台线程何时“看到”主线程对stopRequested的值所做的改变。没有同步，虚拟机将这个代码：

```java
while(!done)
    i++;
```

转变成这样：

```java
if(!done)
    while(true)
        i++;
```

这种优化称作提升（hoisting），正是HopSpot server VM的工作。结果是个活性失败（liveness failure）：这个程序无法前进。修正这个问题的一种方式是同步访问stopRequested域。这个程序会如期般在大约一秒钟之内终止：

```java
//properly synchronized cooperative thread termination
public class StopThread {
    private static boolean stopRequested;
    private static synchronized void requestStop() {
        stopRequested = true;
    }
    private static synchronized boolean stopRequested() {
        return stopRequested;
    }
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(new Runnable() {
            public void run() {
                int i = 0;
                while(!stopRequest()) {
                    i++;
                }
            }
        });
        backgroundThread.start();
        TimeUnit.SECONDS.sleep(1);
        requestStop();
    }
}
```

注意写方法（requestStop）和读方法（stopRequested）都被同步了。只同步写方法还不够。实际上，如果读和写操作没有都被同步，同步就不会起作用。

StopThread中被同步方法的动作即使没有同步也是原子的。换句话说，这些方法的同步只是为了它的通信效果，而不是为了互斥访问。

所以可以使用volatile修饰符。虽然volatile修饰符不执行互斥访问，但它可以保证任何一个线程在读取该域的时候都将看到最近刚刚被写入的值：

```java
//broken! how long would you expect this program to run?
public class StopThread {
    private static volatile boolean stopRequested;
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(new Runnable() {
            public void run() {
                int i = 0;
                while(!stopRequested) {
                    i++;
                }
            }
        });
        backgroundThread.start();
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```

在使用volatile的时候务必要小心：

```java
//broken requires synchronization
private static volatile int nextSerivalNumber = 0;
public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```

增量操作符（++）不是原子的。它在nextSerialNumber域中执行两项操作：首先它读取值，然后写回一个新值，相当于原来的值再加上1.如果第二个线程在第一个线程读取旧值和写回新值期间读取这个域，第二个线程就会与第一个线程一起看到同一个值，并返回相同的序列号。这就是安全性失败：这个程序会计算出错误的结果。

修正generateSerialNumber方法的一种方法是在它的声明中增加synchronized修饰符，这样就可以删除volatile修饰符了。

最好还是用AtomicLong类代替。

避免本条目中所讨论到的问题的最佳办法是不共享可变的数据。将可变数据限制在单个线程中。

安全发布对象引用有许多种方法：可以将它保存在静态域中，作为类初始化的一部分；可以将它保存在volatile域、final域或者通过正常锁定访问的域中；或者可以将它放到并发的集合中。

#### 67. 避免过度同步

依据情况的不同，过度同步可能会导致性能降低、死锁，甚至不确定的行为。

通常，应该在同步区域内做尽可能少的工作。获得锁，检查共享数据，根据需要转换数据，然后放掉锁。如果必须要执行某个很耗时的动作，则应该设法把这个动作移到同步区域的外面。

简而言之，为了避免死锁和数据破坏，千万不要从同步区域内部调用外来方法。更为一般的讲，要尽可能限制同步区域内部的工作量。

#### 68. executor和task优先于线程

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.execute(runnable);
```

为特殊的应用程序选择executor service是很有技巧的。也可以直接使用ThreadPoolExecutor类。

#### 69. 并发工具优先于wait和notify

并发集合为标准的集合接口（如List、Queue和Map）提供了高性能的并发实现。如ConcurrentHashMap、BlockingQueue

同步器是一些使线程能够等待另一个线程的对象，允许它们协调动作。如CountDownLatch、Semaphore、CyclicBarrier、Exchange等。

#### 70. 线程安全性的文档化

简而言之，每个类都应该利用字斟句酌的说明或者线程安全注解，清楚地在文档中说明他的线程安全属性。

#### 71.慎用延迟初始化

简而言之，大多数的域应该正常地进行初始化，而不是延迟初始化。如果为了达到性能目标，或者为了破坏有害的初始化循环，而必须延迟初始化一个域，就可以使用相应的延迟初始化方法。对于实例域，就使用双重检查模式；对于静态域，则使用lazy initialization holder class idiom。对于可以接受重复初始化的实例域，也可以考虑使用单重检查模式

```java
//lazy initialization holder class idiom for static fields
private static class FieldHolder {
    static final FieldType field = computeFieldValue();
}
static FieldType getField() {
    return FieldHolder.field;
}
```

```java
//double-check idiom for lazy initialization of instance fields
private volatile FieldType field;
FieldType getField() {
    FieldType result = field;
    if(result==null) {
        synchronized(this) {
            result = filed;
            if(result==null) {
                filed=result=computeFieldValue();
            }
        }
    }
    return result;
}
```

#### 72. 不要依赖于线程调度器

简而言之，不要让应用程序的正确性依赖于线程调度器。否则，结果得到的应用程序将即不健壮，也不具有可移植性。作为推论，不要依赖Thread.yield或者线程优先级。这些设施仅仅对调度器做些暗示。线程优先级可以用来提高一个已经能够正常工作的程序的服务质量，但永远不应该用来“修正”一个原本并不能工作的程序。

### 十. 序列化

#### 74.谨慎的实现Serializable接口

 #### 75.考虑使用自定义的序列化形式

#### 76.保护性的编写readObject方法

#### 77.对于实例控制，枚举类型优先与readResolve

#### 78.考虑用序列化代理代替序列化实例



