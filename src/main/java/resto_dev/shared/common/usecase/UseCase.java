package resto_dev.shared.common.usecase;

/**
 * Represents a Use Case that receives an Input and returns an Output.
 *
 * @param <I> Input argument type (e.g. Command or Query DTO)
 * @param <O> Output argument type (e.g. Domain Entity or Response DTO)
 */
public interface UseCase<I, O> {
    O execute(I input);
}
