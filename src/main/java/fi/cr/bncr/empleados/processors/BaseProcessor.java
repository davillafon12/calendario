package fi.cr.bncr.empleados.processors;

public interface BaseProcessor<T> {

    public T process(T object);
}
