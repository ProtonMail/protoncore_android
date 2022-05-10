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

import org.jetbrains.annotations.Nullable;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.StringVar;

import java.util.ArrayList;
import java.util.List;

/** Parser for conventional commits.
 * Needs to be written in Java, because Parboiled library is doing some "magic" stuff,
 * which doesn't work with Kotlin.
 */
public class ConventionalCommitParser extends BaseParser<ConventionalCommit> {
    private static final ConventionalCommitParser parser = Parboiled.createParser(ConventionalCommitParser.class);
    private static final ParseRunner<ConventionalCommit> parseRunner = new BasicParseRunner<>(parser.Commit());

    @Nullable
    public static ConventionalCommit parse(String input) {
        ParsingResult<ConventionalCommit> result = parseRunner.run(input);
        if (result.matched) {
            return result.valueStack.pop();
        } else {
            return null;
        }
    }

    public Rule Commit() {
        return Sequence(
                push(new ConventionalCommit()),
                Type(),
                Optional(Scopes()),
                Optional(
                        "!",
                        push(pop().breaking(true))
                ),
                ": ",
                Description(),
                ZeroOrMore(NewLine()),
                Optional(Body()),
                ZeroOrMore(NewLine()),
                Optional(Footers()),
                ZeroOrMore(NewLine()),
                EOI
        );
    }

    public Rule Body() {
        return Sequence(
                Sequence(
                        TestNot(FooterKey()),
                        OneOrMore(NoneOf("\n")),
                        ZeroOrMore(
                                OneOrMore(NewLine()),
                                TestNot(FooterKey()),
                                OneOrMore(NoneOf("\n"))
                        )
                ),
                push(pop().body(match()))
        );
    }

    public Rule Description() {
        return Sequence(
                OneOrMore(NoneOf("\n")),
                push(pop().description(match()))
        );
    }

    public Rule Footers() {
        return OneOrMore(
                Footer(),
                ZeroOrMore(NewLine())
        );
    }

    public Rule Footer() {
        StringVar footerKeyName = new StringVar();
        return Sequence(
                FooterKey(),
                footerKeyName.set(match().substring(0, match().length() - 2)), // `substring` removes footer delimiter
                Sequence(
                        OneOrMore(NoneOf("\n")),
                        ZeroOrMore(
                                ZeroOrMore(NewLine()),
                                TestNot(FooterKey()),
                                OneOrMore(NoneOf("\n"))
                        )
                ),
                push(pop().appendFooter(new CommitFooter(footerKeyName.get(), match())))
        );
    }

    public Rule FooterKey() {
        return Sequence(
                FirstOf(
                        Sequence("BREAKING CHANGE", push(pop().breaking(true))),
                        Sequence("BREAKING-CHANGE", push(pop().breaking(true))),
                        OneOrMore(TestNot(FooterDelimiter()), NoneOf(" \n"))
                ),
                FooterDelimiter()
        );
    }

    public Rule FooterDelimiter() {
        return FirstOf(": ", " #");
    }

    public Rule NewLine() {
        return Ch('\n');
    }

    public Rule Scopes() {
        return Sequence(
                Ch('('),
                OneOrMore(Scope()),
                Ch(')')
        );
    }

    public Rule Scope() {
        return Sequence(
                ZeroOrMore(" "),
                OneOrMore(NoneOf(",)")),
                push(pop().appendScope(match())),
                ZeroOrMore(AnyOf(" ,"))
        );
    }

    public Rule Type() {
        return Sequence(
                OneOrMore(NoneOf(" !(:")),
                push(pop().type(match()))
        );
    }

    protected List<CommitFooter> append(List<CommitFooter> list, CommitFooter footer) {
        ArrayList<CommitFooter> arrayList = new ArrayList<>(list);
        arrayList.add(footer);
        return arrayList;
    }
}
