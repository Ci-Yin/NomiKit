package ciyin.parser.util

import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.core.movie.MovieParserType
import ciyin.parser.core.movie.model.MovieRequest
import ciyin.parser.core.movie.model.MovieResult
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.scope.ParserScope


typealias PictureParserScope = ParserScope<PictureParserType, PictureRequest, PictureResult>

typealias ComicParserScope = ParserScope<ComicParserType, ComicRequest, ComicResult>

typealias MovieParserScope = ParserScope<MovieParserType, MovieRequest, MovieResult>
