package net.brentwalther.javacash.lanterna;

import com.googlecode.lanterna.gui2.Component;
import io.reactivex.rxjava3.core.Observable;

import java.util.Optional;

public interface OptionalComponent {
  Observable<Optional<Component>> observable();
}
