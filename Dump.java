import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Dump {
    public static void main(String[] args) {
        System.out.println("== KineticBlockEntity ==");
        for (Field f : KineticBlockEntity.class.getDeclaredFields()) {
            System.out.println(f.getName() + " " + f.getType().getName());
        }
        for (Method m : KineticBlockEntity.class.getDeclaredMethods()) {
            if(m.getName().toLowerCase().contains("stress") || m.getName().toLowerCase().contains("capacity"))
                System.out.println(m.getName() + " " + m.getReturnType().getName());
        }
        System.out.println("== GeneratingKineticBlockEntity ==");
        for (Method m : com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity.class.getDeclaredMethods()) {
            if(m.getName().toLowerCase().contains("stress") || m.getName().toLowerCase().contains("capacity"))
                System.out.println(m.getName() + " " + m.getReturnType().getName());
        }
    }
}
