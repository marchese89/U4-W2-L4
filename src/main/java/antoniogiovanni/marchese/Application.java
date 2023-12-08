package antoniogiovanni.marchese;

import antoniogiovanni.marchese.catalogo.Customer;
import antoniogiovanni.marchese.catalogo.Order;
import antoniogiovanni.marchese.catalogo.Product;
import com.github.javafaker.Faker;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) {

        Supplier<Product> productSupplier = () -> {
            Faker faker = new Faker(Locale.ITALY);
            return new Product(faker.name().firstName(),faker.cat().name(),faker.random().nextDouble()*40);
        };
        //creo un po' di prodotti a caso
        List<Product> listaProdotti = new ArrayList<>();

        for(int i = 0; i < 100; i++){
            listaProdotti.add(productSupplier.get());
        }
//        System.out.println("Lista prodotti");
//        listaProdotti.forEach(System.out::println);

        //creo un po' di Customer
        Supplier<Customer> customerSupplier = () -> {
            Faker faker = new Faker(Locale.ITALY);
            return new Customer(faker.name().name(),faker.random().nextInt(1,4).intValue());
        };
        List<Customer> listaCustomer = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            listaCustomer.add(customerSupplier.get());
        }
//        System.out.println("Lista clienti");
//        listaCustomer.forEach(System.out::println);

        //creo un po' di ordini
        Supplier<Order> orderSupplier = () -> {
            Faker faker = new Faker(Locale.ITALY);

            Order o = new Order(faker.name().name(),listaCustomer.get(faker.random().nextInt(0,9).intValue()));

            List<Product> products = o.getProducts();

            for (int i = 0; i < 6; i++){
                products.add(listaProdotti.get(faker.random().nextInt(0,99)));
            }

            return o;
        };

        List<Order> listaOrdini = new ArrayList<>();

        for (int i = 0; i < 30; i++){
            listaOrdini.add(orderSupplier.get());
        }
//        System.out.println("Lista ordini");
//        listaOrdini.forEach(System.out::println);

        System.out.println("-------------------------------------- EX1 ----------------------------------------");
        /*
        Raggruppare gli ordini per cliente utilizzando Stream e Lambda Expressions. Crea una mappa in cui la chiave è il cliente \
        e il valore è una lista di ordini effettuati da quel cliente
         */
        Map<Customer,List<Order>> ordiniPerCliente = listaOrdini.stream().collect(Collectors.groupingBy(Order::getCustomer));
        ordiniPerCliente.forEach(((customer, orders) -> {System.out.println(customer);orders.forEach(System.out::println);}));

        System.out.println("--------------------------------------- EX2 ----------------------------------------");
        /*
        Dato un elenco di ordini, calcola il totale delle vendite per ogni cliente utilizzando Stream e Lambda Expressions.
        Crea una mappa in cui la chiave è il cliente e il valore è l'importo totale dei suoi acquisti
         */
        //Map<Customer,Double> acquistiCliente = listaOrdini.stream().collect(Collectors.groupingBy(Order::getCustomer,Collectors.reducing(0.0,Order::getTotal,Double::sum)));
        //                                                                               raggruppa per Customer ---------------- somma i double ottenuti dagli ordini --- prendiamo i prezzi dei prodotti degli ordini e li sommiamo
        Map<Customer,Double> aquistiClienteAlt = listaOrdini.stream().collect(Collectors.groupingBy(Order::getCustomer,Collectors.summingDouble(order-> order.getProducts().stream().mapToDouble(Product::getPrice).sum())));

        aquistiClienteAlt.forEach(((customer, aDouble) -> {
            System.out.println(customer+" "+aDouble);
        }));

        System.out.println("---------------------------------------- EX3 ----------------------------------------");
        /*
        Dato un elenco di prodotti, trova i prodotti più costosi utilizzando Stream e Lambda Expressions
         */
        //prodotti più costosi
//        double maxPrice = listaProdotti.stream().mapToDouble(Product::getPrice).max().getAsDouble();
        List<Product> prodottiCostosi = listaProdotti.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).limit(3).toList();
        prodottiCostosi.forEach(System.out::println);

        System.out.println("----------------------------------------- EX4 -----------------------------------------");
        /*
        Dato un elenco di ordini, calcola la media degli importi degli ordini utilizzando Stream e Lambda Expressions
         */
        double media = listaOrdini.stream().mapToDouble(order -> order.getProducts().stream().mapToDouble(Product::getPrice).sum()).average().getAsDouble();

        System.out.println("media importi degli ordini: "+media);

        System.out.println("---------------------------------------- EX5 --------------------------------------------");
        /*
        Dato un elenco di prodotti, raggruppa i prodotti per categoria e calcola la somma degli importi per ogni categoria
        utilizzando Stream e Lambda Expressions
         */
        Map<String,Double> importiPerCategoria = listaProdotti.stream().collect(Collectors.groupingBy(Product::getCategory,Collectors.summingDouble(Product::getPrice)));
        importiPerCategoria.forEach(((cat, aDouble) -> {
            System.out.print("Categoria: "+cat);
            System.out.println(" , somma importi: "+aDouble);
        }));
        System.out.println("------------------------------------------- EX6 ---------------------------------------------");
        /*
        Usando la classe Apache Commons IO FileUtils implementare un metodo salvaProdottiSuDisco che salvi su disco un file
        contenente la lista dei prodotti. Utilizzare un formato simile al seguente per storicizzare i dati su file:
        nomeprodotto1@categoriaprodotto1@prezzoprodotto1#nomeprodotto2@categoriaprodotto2@prezzoprodotto2
         */
        try {
            salvaProdottiSuDisco(listaProdotti);
//            salvaProdottiSuDiscoAlternativa(listaProdotti);
            System.out.println("file scritti su disco");
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------ EX7 ---------------------------------------------");
        List<Product> listaLettaDaDisco = null;

        try {
            listaLettaDaDisco = leggiProdottiDaDisco();
//            listaLettaDaDisco = leggiProdottiDaDiscoAlternativa();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("---------------------------------- LISTA LETTA DA DISCO ----------------------------------------");
        listaLettaDaDisco.forEach(System.out::println);
    }

    public static void salvaProdottiSuDisco(List<Product> l) throws IOException {
        File f = new File("store.txt");
        FileUtils.writeStringToFile(f,"",false);
        for(Product p: l){
            FileUtils.writeStringToFile(f,p.getName()+"@"+p.getCategory()+"@"+p.getPrice() + System.lineSeparator(), StandardCharsets.UTF_8,true);
        }

    }

    public static List<Product> leggiProdottiDaDisco() throws IOException {
        List<Product> res = new LinkedList<>();
        File f = new File("store.txt");
        String filesString = FileUtils.readFileToString(f,StandardCharsets.UTF_8);
        StringTokenizer stk = new StringTokenizer(filesString,System.lineSeparator());
        while (stk.hasMoreTokens()){
            String prod = stk.nextToken();
            StringTokenizer ss2 = new StringTokenizer(prod,"@");
            String nome = ss2.nextToken();
            String categoria = ss2.nextToken();
            double prezzo = Double.parseDouble(ss2.nextToken());

            Product p = new Product(nome,categoria,prezzo);
            res.add(p);
        }

        return res;
    }

    public static void salvaProdottiSuDiscoAlternativa(List<Product> l) throws IOException {
        File f = new File("store.txt");

        PrintWriter pw = new PrintWriter(f);
        pw.write("");
        for (Product p: l){
            pw.append(p.getName()+"@"+p.getCategory()+"@"+p.getPrice() + System.lineSeparator());
        }
        pw.close();
    }

    public static List<Product> leggiProdottiDaDiscoAlternativa() throws IOException {
        List<Product> res = new LinkedList<>();
        File f = new File("store.txt");
        Scanner sc = new Scanner(f);

        while (sc.hasNextLine()){
            String prod = sc.nextLine();
            StringTokenizer ss2 = new StringTokenizer(prod,"@");
            String nome = ss2.nextToken();
            String categoria = ss2.nextToken();
            double prezzo = Double.parseDouble(ss2.nextToken());

            Product p = new Product(nome,categoria,prezzo);
            res.add(p);
        }

        return res;
    }
}
