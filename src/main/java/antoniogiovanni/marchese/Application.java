package antoniogiovanni.marchese;

import antoniogiovanni.marchese.catalogo.Customer;
import antoniogiovanni.marchese.catalogo.Order;
import antoniogiovanni.marchese.catalogo.Product;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

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
        System.out.println("Lista prodotti");
        listaProdotti.forEach(System.out::println);

        //creo un po' di Customer
        Supplier<Customer> customerSupplier = () -> {
            Faker faker = new Faker(Locale.ITALY);
            return new Customer(faker.name().name(),faker.random().nextInt(1,4).intValue());
        };
        List<Customer> listaCustomer = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            listaCustomer.add(customerSupplier.get());
        }
        System.out.println("Lista clienti");
        listaCustomer.forEach(System.out::println);

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
        System.out.println("Lista ordini");
        listaOrdini.forEach(System.out::println);
    }
}
