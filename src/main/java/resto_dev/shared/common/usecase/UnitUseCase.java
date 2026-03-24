package resto_dev.shared.common.usecase;

/**
 * Represents a Use Case that receives an Input but does not return anything.
 *
 * @param <I> Input argument type
 */
public interface UnitUseCase<I> {
    void execute(I input);
}
