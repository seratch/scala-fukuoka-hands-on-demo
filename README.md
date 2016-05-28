## Skinny Workshop at Scala 福岡 2016

Scala 福岡 2016

http://scala.connpass.com/event/26674/

### How to run

```
git clone https://github.com/seratch/scala-fukuoka-hands-on-demo.git
cd scala-fukuoka-hands-on-demo
skinny db:migrate
skinny run
```

### What this example does

```
skinny new todolist
cd todolist
skinny g scaffold tasks task title:String description:Option[String] deadline:LocalDate isDone:Boolean
skinny db:migrate
skinny run
# change the default locale (0d8633c254ffaa251a60e4e8678f171ead6359a1)
```

