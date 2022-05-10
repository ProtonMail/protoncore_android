/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.conventionalcommits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConventionalCommit {
    public final String body;
    public final boolean breaking;
    public final String description;
    public final List<CommitFooter> footers;
    public final List<String> scopes;
    public final String type;

    public ConventionalCommit() {
        this("", Collections.emptyList(), "", "", Collections.emptyList(), false);
    }

    public ConventionalCommit(String type, List<String> scopes, String description, String body, List<CommitFooter> footers, boolean breaking) {
        this.type = type;
        this.scopes = scopes;
        this.description = description;
        this.body = body;
        this.footers = footers;
        this.breaking = breaking;
    }

    public ConventionalCommit appendScope(String newScope) {
        final List<String> newScopes = new ArrayList<>(scopes);
        newScopes.add(newScope);
        return new ConventionalCommit(type, newScopes, description, body, footers, breaking);
    }

    public ConventionalCommit appendFooter(CommitFooter newFooter) {
        final List<CommitFooter> newFooters = new ArrayList<>(footers);
        newFooters.add(newFooter);
        return new ConventionalCommit(type, scopes, description, body, newFooters, breaking);
    }

    public ConventionalCommit type(String newType) {
        return new ConventionalCommit(newType, scopes, description, body, footers, breaking);
    }

    public ConventionalCommit scopes(List<String> newScopes) {
        return new ConventionalCommit(type, newScopes, description, body, footers, breaking);
    }

    public ConventionalCommit description(String newDescription) {
        return new ConventionalCommit(type, scopes, newDescription, body, footers, breaking);
    }

    public ConventionalCommit body(String newBody) {
        return new ConventionalCommit(type, scopes, description, newBody, footers, breaking);
    }

    public ConventionalCommit footers(List<CommitFooter> newFooters) {
        return new ConventionalCommit(type, scopes, description, body, newFooters, breaking);
    }

    public ConventionalCommit breaking(boolean newBreaking) {
        return new ConventionalCommit(type, scopes, description, body, footers, newBreaking);
    }

    @Override
    public String toString() {
        return "ConventionalCommit{" +
                "type='" + type + '\'' +
                ", scopes='" + scopes + '\'' +
                ", description='" + description + '\'' +
                ", body='" + body + '\'' +
                ", footers=" + footers +
                '}';
    }
}
