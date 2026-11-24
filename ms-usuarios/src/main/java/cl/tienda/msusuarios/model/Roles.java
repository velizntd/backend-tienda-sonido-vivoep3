package cl.tienda.msusuarios.model;

import java.util.Set;

public final class Roles {

    public static final String CLIENTE = "cliente";
    public static final String VENDEDOR = "vendedor";
    public static final String ADMINISTRADOR = "administrador";

    public static final Set<String> VALIDOS = Set.of(CLIENTE, VENDEDOR, ADMINISTRADOR);

    private Roles() {
    }
}
