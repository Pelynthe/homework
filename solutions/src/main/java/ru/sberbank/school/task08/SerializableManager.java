package ru.sberbank.school.task08;

import lombok.NonNull;
import ru.sberbank.school.task08.state.GameObject;
import ru.sberbank.school.task08.state.InstantiatableEntity;
import ru.sberbank.school.task08.state.MapState;
import ru.sberbank.school.util.Solution;

import java.io.*;
import java.util.List;

import static java.io.File.separator;
import static ru.sberbank.school.task08.SaveGameException.Type.IO;

@Solution(8)
public class SerializableManager extends SaveGameManager<MapState<GameObject>, GameObject> {

    /**
     * Конструктор не меняйте.
     */
    public SerializableManager(@NonNull String filesDirectoryPath) {
        super(filesDirectoryPath);
    }

    @Override
    public void initialize() {
//        throw new UnsupportedOperationException("Implement me!");
    }

    @Override
    public void saveGame(@NonNull String filename, @NonNull MapState<GameObject> gameState) throws SaveGameException {
        String path = filesDirectory + separator + filename;

        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {

            outputStream.writeObject(gameState);

        } catch (IOException | NullPointerException e) {
            throw new SaveGameException("SerializableManager Saving error", e, IO, gameState);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapState<GameObject> loadGame(@NonNull String filename) throws SaveGameException {
        String path = filesDirectory + separator + filename;

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {

            MapState<GameObject> saved = (MapState<GameObject>) inputStream.readObject();
            return saved;

        } catch (IOException | NullPointerException | ClassNotFoundException | ClassCastException e) {
            throw new SaveGameException("SerializableManager Loading error", e, IO, null);
        }
    }

    @Override
    public GameObject createEntity(InstantiatableEntity .Type type,
                                             InstantiatableEntity .Status status,
                                             long hitPoints) {
        return new GameObject(type, status, hitPoints);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapState<GameObject> createSavable(String name, List<GameObject> entities) {
        return new MapState<GameObject>(name, entities);
    }
}