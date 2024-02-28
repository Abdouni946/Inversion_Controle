# Rapport d'activité N° 1 : Inversion de côntrole et injection de dépendances

### Filiére: Génie Logiciel des Systèmes informatiques distribués

### Encadrant: M. Mohammed YOUSSFI

### Réalisé par: Zakariae ABDOUNI

### Part 1

#### Create the IDao interface with a method getDate :
```java
package Part1.Dao;

public interface IDao {
    public String getDate();
}

```
#### Create an implementation of this interface :
```java
package Part1.Dao;

import org.springframework.stereotype.Component;

@Component("dao")
public class DaoImp implements IDao{
    @Override
    public String getDate() {
        return new java.util.Date().toString();
    }
}

```
#### Create the IMetier interface with a method called calculate :
```java
package Part1.Metier;

public interface IMetier {
    public int calcul();
}

```
#### Create an implementation of this interface using loose coupling :
```java
package Part1.Metier;

import Part1.Dao.IDao;
import org.springframework.stereotype.Component;

@Component("metier")
public class MetierImp implements IMetier{

    private IDao dao; //couplage faible

    public MetierImp(IDao dao) {
        this.dao = dao;
    }

    public void setDao(IDao dao) {
        this.dao = dao;
    }
    public int calcul() {
        String date = dao.getDate();

        return 30 - Integer.parseInt(date.substring(8, 10));
    }

    // Injection dans la variable dao un objet d'une class qui implémente l'interface IDao
    public void SetDao(IDao dao) {
        this.dao = dao;
    }

}

```
#### Perform dependency injection through static instantiation :
```java
package Part1.Presentation;

import Part1.Dao.DaoImp;
import Part1.Metier.MetierImp;

public class Main {
    public static void main(String[] args) {

        DaoImp dao = new DaoImp();
        MetierImp metier = new MetierImp(dao);

        String days = metier.calcul() + " jours restants";
        System.out.println("resultat avec injection statique = "
                + days);
    }
}

```
#### Perform dependency injection through dynamic instantiation:
```java
package Part2;

import Part1.Dao.DaoImp;
import Part1.Metier.IMetier;
import Part1.Metier.MetierImp;

public class App {
    public static void main(String[] args) {
        IoCContainer container = new IoCContainer();

        //Saving the beans in a container
        container.registerBean("dao", new DaoImp());
        container.registerBean("metier", new MetierImp(new DaoImp()));

        // Injection of dependencies
        container.doConstructorInjection();
        container.doSetterInjection();
        container.doFieldInjection();

        //Using our beans
        IMetier metier = (IMetier) container.getBean("metier");
        metier.calcul();
    }
}
```
#### Perform dependency injection using the Spring Framework with XML configuration:
##### applicationContext.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="dao" class="Part1.Dao.DaoImp"></bean>
    <bean id="metier" class="Part1.Metier.MetierImp">
        <constructor-arg ref="dao"></constructor-arg>
    </bean>

</beans>
```
##### app.java
```java
package Part1.Presentation;

import Part1.Metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringXML {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        IMetier metier = (IMetier) context.getBean("metier");
        String days = metier.calcul() + " jours restants";
        System.out.println("resultat avec Spring XML = " + days);
        ((ClassPathXmlApplicationContext) context).close();
    }
}

```
#### Perform dependency injection using the Spring Framework with annotations:
```java
package Part1.Presentation;

import Part1.Dao.IDao;
import Part1.Metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringAnnotation {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("Dao", "Metier");
        IMetier metier = context.getBean(IMetier.class);
        String days = metier.calcul() + " jours restants";
        System.out.println("resultat avec Spring Annotation  = " + days);

    }
}

```

### Part 2

#### The IOC
````java
package Part2;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@interface Autowired {
}

public class IoCContainer {
    private Map<String, Object> beans = new HashMap<>();

    public void registerBean(String beanName, Object bean) {
        beans.put(beanName, bean);
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    public void doConstructorInjection() {
        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();
            for (Constructor<?> constructor : beanClass.getConstructors()) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        parameters[i] = beans.get(parameterTypes[i].getSimpleName());
                    }
                    try {
                        constructor.newInstance(parameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void doSetterInjection() {
        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();
            for (Method method : beanClass.getMethods()) {
                if (method.getName().startsWith("set") && method.isAnnotationPresent(Autowired.class)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        continue; // Ignorer les méthodes setter avec plus d'un paramètre
                    }
                    Object dependency = beans.get(parameterTypes[0].getSimpleName());
                    try {
                        method.invoke(bean, dependency);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void doFieldInjection() {
        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dependency = beans.get(field.getType().getSimpleName());
                    field.setAccessible(true);
                    try {
                        field.set(bean, dependency);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
````
#### The implementation of the framework
````java
package Part2;

import Part1.dao.DaoImp;
import Part1.metier.IMetier;
import Part1.metier.MetierImp;

public class App {
    public static void main(String[] args) {
        IoCContainer container = new IoCContainer();
        
        //Saving the beans in a container
        container.registerBean("dao", new DaoImp());
        container.registerBean("metier", new MetierImp(new DaoImp()));

        // Injection of dependencies
        container.doConstructorInjection();
        container.doSetterInjection();
        container.doFieldInjection();

        //Using our beans
        IMetier metier = (IMetier) container.getBean("metier");
        metier.calcule();
    }
}
````
