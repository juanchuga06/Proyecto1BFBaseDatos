public class ItemMedicamento {
    private int id;
    private String nombre;
    private String presentacion;

    public ItemMedicamento(int id, String nombre, String presentacion) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
    }

    public int getId() { return id; }

    @Override
    public String toString() {
        return nombre + " - " + presentacion;
    }
}