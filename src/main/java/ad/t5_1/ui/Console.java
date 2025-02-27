package ad.t5_1.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

import ad.t5_1.dao.*;
import ad.t5_1.models.*;

/**
 * Implementación de la interfaz de usuario en modo consola.
 * Adaptada para trabajar con la implementación basada en Hibernate.
 */
public class Console implements UserInterface {
    private final Scanner scanner;
    private final ClienteDAO clienteDAO;
    private final PedidoDAO pedidoDAO;
    private final ZonaEnvioDAO zonaDAO;
    private final DateTimeFormatter dateFormatter;
    private boolean exit;

    /**
     * Constructor que inicializa los componentes necesarios para la interfaz.
     */
    public Console() {
        this.scanner = new Scanner(System.in);
        this.clienteDAO = new ClienteDAO();
        this.pedidoDAO = new PedidoDAO();
        this.zonaDAO = new ZonaEnvioDAO();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    /**
     * Muestra el menú principal y sus opciones.
     */
    private void mostrarMenuPrincipal() {
        System.out.println("\n=== GESTIÓN DE PEDIDOS ===");
        System.out.println("1. Gestión de Clientes");
        System.out.println("2. Gestión de Pedidos");
        System.out.println("3. Consultar Zonas de Envío");
        System.out.println("4. Consultar Pedidos de Cliente");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    /**
     * Gestiona el submenú de clientes y sus operaciones.
     */
    private void gestionClientes() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== GESTIÓN DE CLIENTES ===");
            System.out.println("1. Ver todos los clientes");
            System.out.println("2. Añadir cliente");
            System.out.println("3. Modificar cliente");
            System.out.println("4. Eliminar cliente");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerOpcion()) {
                case 1 -> mostrarClientes();
                case 2 -> agregarCliente();
                case 3 -> modificarCliente();
                case 4 -> eliminarCliente();
                case 0 -> exit = true;
                default -> System.out.println("Opción no válida");
            }
        }
    }

    /**
     * Gestiona el submenú de pedidos y sus operaciones.
     */
    private void gestionPedidos() {
        while (true) {
            System.out.println("\n=== GESTIÓN DE PEDIDOS ===");
            System.out.println("1. Ver todos los pedidos");
            System.out.println("2. Añadir pedido");
            System.out.println("3. Modificar pedido");
            System.out.println("4. Eliminar pedido");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            switch (leerOpcion()) {
                case 1 -> mostrarPedidos();
                case 2 -> agregarPedido();
                case 3 -> modificarPedido();
                case 4 -> eliminarPedido();
                case 0 -> { return; }
                default -> System.out.println("Opción no válida");
            }
        }
    }

    /**
     * Muestra la lista de todos los clientes registrados.
     */
    private void mostrarClientes() {
        System.out.println("\n=== LISTADO DE CLIENTES ===");
        clienteDAO.getAll().forEach(cliente -> 
            System.out.printf("ID: %d, Nombre: %s, Email: %s, Teléfono: %s, Zona: %d%n",
                cliente.getId(), 
                cliente.getNombre(), 
                cliente.getEmail(), 
                cliente.getTelefono(),
                cliente.getIdZona())
        );
    }

    /**
     * Gestiona el proceso de agregar un nuevo cliente.
     * Solicita los datos necesarios al usuario y los valida.
     */
    private void agregarCliente() {
        System.out.println("\n=== AÑADIR CLIENTE ===");
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();
        
        System.out.println("\nZonas de envío disponibles:");
        zonaDAO.getAll().forEach(zona -> 
            System.out.printf("%d - %s (Tarifa: %.2f€)%n", 
                zona.getId(), 
                zona.getNombre(), 
                zona.getTarifa())
        );
        
        System.out.print("ID de la zona: ");
        int idZona = Integer.parseInt(scanner.nextLine());
        Optional<ZonaEnvio> zonaOpt = zonaDAO.getById(idZona);

        if(zonaOpt.isPresent()){
            Cliente cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setEmail(email);
            cliente.setTelefono(telefono);
            cliente.setZona(zonaOpt.get());
            
            clienteDAO.save(cliente);
            System.out.println("Cliente añadido con éxito.");
        }
    }

    /**
     * Gestiona el proceso de modificación de un cliente existente.
     * Permite modificar campos individuales manteniendo los demás sin cambios.
     */
    private void modificarCliente() {
        System.out.println("\n=== MODIFICAR CLIENTE ===");
        System.out.print("ID del cliente a modificar: ");
        int id = Integer.parseInt(scanner.nextLine());

        Optional<Cliente> clienteOpt = clienteDAO.getById(id);
        if (clienteOpt.isEmpty()) {
            System.out.println("Cliente no encontrado.");
            return;
        }

        Cliente cliente = clienteOpt.get();
        System.out.printf("Nombre actual: %s%nNuevo nombre (Enter para mantener): ", cliente.getNombre());
        String nombre = scanner.nextLine();
        if (!nombre.isEmpty()) cliente.setNombre(nombre);

        System.out.printf("Email actual: %s%nNuevo email (Enter para mantener): ", cliente.getEmail());
        String email = scanner.nextLine();
        if (!email.isEmpty()) cliente.setEmail(email);

        System.out.printf("Teléfono actual: %s%nNuevo teléfono (Enter para mantener): ", cliente.getTelefono());
        String telefono = scanner.nextLine();
        if (!telefono.isEmpty()) cliente.setTelefono(telefono);

        clienteDAO.save(cliente);
        System.out.println("Cliente modificado con éxito.");
    }

    /**
     * Gestiona el proceso de eliminación de un cliente.
     */
    private void eliminarCliente() {
        System.out.println("\n=== ELIMINAR CLIENTE ===");
        System.out.print("ID del cliente a eliminar: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (clienteDAO.delete(id)) {
            System.out.println("Cliente eliminado con éxito.");
        } else {
            System.out.println("Cliente no encontrado.");
        }
    }

    /**
     * Muestra la lista de todos los pedidos registrados.
     */
    private void mostrarPedidos() {
        System.out.println("\n=== LISTADO DE PEDIDOS ===");
        pedidoDAO.getAll().forEach(pedido -> 
            System.out.printf("ID: %d, Fecha: %s, Importe: %.2f€, ID Cliente: %d%n",
                pedido.getId(),
                pedido.getFecha().format(dateFormatter),
                pedido.getImporteTotal(),
                pedido.getIdCliente())
        );
    }

    /**
     * Gestiona el proceso de agregar un nuevo pedido.
     * Valida la existencia del cliente antes de crear el pedido.
     */
    private void agregarPedido() {
        System.out.println("\n=== AÑADIR PEDIDO ===");
        System.out.print("ID del cliente: ");
        int idCliente = Integer.parseInt(scanner.nextLine());
    
        Optional<Cliente> clienteOpt = clienteDAO.getById(idCliente);
        if (clienteOpt.isEmpty()) {
            System.out.println("Cliente no encontrado.");
            return;
        }
    
        System.out.print("Importe total: ");
        double importe = Double.parseDouble(scanner.nextLine());
    
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteOpt.get()); // Establecer el cliente directamente
        pedido.setFecha(LocalDate.now());
        pedido.setImporteTotal(importe);
    
        pedidoDAO.save(pedido);
        System.out.println("Pedido añadido con éxito.");
    }

    /**
     * Gestiona el proceso de modificación de un pedido existente.
     */
    private void modificarPedido() {
        System.out.println("\n=== MODIFICAR PEDIDO ===");
        System.out.print("ID del pedido a modificar: ");
        int id = Integer.parseInt(scanner.nextLine());

        Optional<Pedido> pedidoOpt = pedidoDAO.getById(id);
        if (pedidoOpt.isEmpty()) {
            System.out.println("Pedido no encontrado.");
            return;
        }

        Pedido pedido = pedidoOpt.get();
        System.out.printf("Importe actual: %.2f€%nNuevo importe (Enter para mantener): ", 
            pedido.getImporteTotal());
        String importeStr = scanner.nextLine();
        if (!importeStr.isEmpty()) {
            pedido.setImporteTotal(Double.parseDouble(importeStr));
        }

        pedidoDAO.save(pedido);
        System.out.println("Pedido modificado con éxito.");
    }

    /**
     * Gestiona el proceso de eliminación de un pedido.
     */
    private void eliminarPedido() {
        System.out.println("\n=== ELIMINAR PEDIDO ===");
        System.out.print("ID del pedido a eliminar: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (pedidoDAO.delete(id)) {
            System.out.println("Pedido eliminado con éxito.");
        } else {
            System.out.println("Pedido no encontrado.");
        }
    }

    /**
     * Muestra información sobre las zonas de envío y el número de clientes en cada zona.
     */
    private void consultarZonasEnvio() {
        System.out.println("\n=== ZONAS DE ENVÍO ===");
        System.out.println("Listado de zonas con número de clientes:");
        zonaDAO.getZonasConNumeroClientes().forEach(zona -> 
            System.out.printf("ID: %d, Nombre: %s, Tarifa: %.2f€%n",
                zona.getId(),
                zona.getNombre(),
                zona.getTarifa()
            )
        );
    }

    /**
     * Muestra los pedidos y el total gastado por un cliente específico.
     */
    private void consultarPedidosCliente() {
        System.out.println("\n=== CONSULTAR PEDIDOS DE CLIENTE ===");
        System.out.print("ID del cliente: ");
        int idCliente = Integer.parseInt(scanner.nextLine());

        Optional<Cliente> clienteOpt = clienteDAO.getById(idCliente);
        if (clienteOpt.isEmpty()) {
            System.out.println("Cliente no encontrado.");
            return;
        }

        Cliente cliente = clienteOpt.get();
        System.out.printf("%nPedidos del cliente %s:%n", cliente.getNombre());
        
        pedidoDAO.getByCliente(idCliente).forEach(pedido ->
            System.out.printf("ID: %d, Fecha: %s, Importe: %.2f€%n",
                pedido.getId(),
                pedido.getFecha().format(dateFormatter),
                pedido.getImporteTotal())
        );

        double totalGastado = pedidoDAO.getTotalGastadoPorCliente(idCliente);
        System.out.printf("%nTotal gastado por el cliente: %.2f€%n", totalGastado);
    }

    /**
     * Lee y parsea una opción numérica del usuario.
     * @return el número introducido o -1 si no es válido
     */
    private int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Inicia la interfaz de usuario y ejecuta el bucle principal del programa.
     */
    @Override
    public void start() {
        exit = false;
        while (!exit) {
            mostrarMenuPrincipal();
            int opcion = leerOpcion();
            
            switch (opcion) {
                case 1 -> gestionClientes();
                case 2 -> gestionPedidos();
                case 3 -> consultarZonasEnvio();
                case 4 -> consultarPedidosCliente();
                case 0 -> stop();
                default -> System.out.println("Opción no válida");
            }
        }
    }

    /**
     * Detiene la ejecución del programa y libera recursos.
     */
    @Override
    public void stop() {
        exit = true;
        if (scanner != null) {
            scanner.close();
        }
    }
}