/* SPDX-License-Identifier: MIT */

import org.jspecify.annotations.NullMarked;

@NullMarked
module atlantafx.sampler {
    requires static org.jspecify;

    requires atlantafx.base;
    requires javafx.fxml;

    requires java.sql;
    requires java.net.http;
    requires com.google.gson;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    exports atlantafx.sampler.zenith;
    opens atlantafx.sampler.zenith to javafx.fxml;
}
