import Foundation
import SwiftSoup

@objcMembers
public final class KmpSoupBridgeParser: NSObject {

    public static func parseHtml(_ html: String) -> KmpSoupBridgeDocument? {
        do {
            let document = try SwiftSoup.parse(html)
            return KmpSoupBridgeDocument(document: document)
        } catch {
            return nil
        }
    }
}

@objcMembers
public final class KmpSoupBridgeDocument: NSObject {

    private let delegate: Document

    fileprivate init(document: Document) {
        self.delegate = document
        super.init()
    }

    public func select(_ css: String) -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.select(css).array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func selectFirst(_ css: String) -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.select(css).first()
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func getElementById(_ elementId: String) -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.getElementById(elementId)
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func getElementsByTag(_ tag: String) -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.getElementsByTag(tag).array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func getElementsByClass(_ className: String) -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.getElementsByClass(className).array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func title() -> String {
        do {
            return try delegate.title()
        } catch {
            return ""
        }
    }

    public func setTitle(_ title: String) {
        do {
            try delegate.title(title)
        } catch {
        }
    }

    public func body() -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.body()
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func setText(_ text: String) {
        do {
            _ = try delegate.text(text)
        } catch {
        }
    }

    public func html() -> String {
        do {
            return try delegate.html()
        } catch {
            return ""
        }
    }

    public func outerHtml() -> String {
        do {
            return try delegate.outerHtml()
        } catch {
            return ""
        }
    }
}

@objcMembers
public final class KmpSoupBridgeElement: NSObject {

    private let delegate: Element

    fileprivate init(element: Element) {
        self.delegate = element
        super.init()
    }

    public func select(_ css: String) -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.select(css).array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func selectFirst(_ css: String) -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.select(css).first()
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func getElementById(_ elementId: String) -> KmpSoupBridgeElement? {
        return selectFirst("#\(elementId)")
    }

    public func getElementsByTag(_ tag: String) -> KmpSoupBridgeElements {
        return select(tag)
    }

    public func getElementsByClass(_ className: String) -> KmpSoupBridgeElements {
        return select(".\(className)")
    }

    public func text() -> String {
        do {
            return try delegate.text()
        } catch {
            return ""
        }
    }

    public func ownText() -> String {
        do {
            return try delegate.ownText()
        } catch {
            return ""
        }
    }

    public func attr(_ name: String) -> String {
        do {
            return try delegate.attr(name)
        } catch {
            return ""
        }
    }

    public func hasAttr(_ name: String) -> Bool {
        do {
            return try delegate.hasAttr(name)
        } catch {
            return false
        }
    }

    public func setAttrName(_ name: String, value: String) {
        do {
            _ = try delegate.attr(name, value)
        } catch {
        }
    }

    public func removeAttrName(_ name: String) {
        do {
            _ = try delegate.removeAttr(name)
        } catch {
        }
    }

    public func id() -> String {
        return attr("id")
    }

    public func tagName() -> String {
        do {
            return try delegate.tagName()
        } catch {
            return ""
        }
    }

    public func className() -> String {
        return attr("class")
    }

    public func hasClassName(_ className: String) -> Bool {
        do {
            return try delegate.hasClass(className)
        } catch {
            return false
        }
    }

    public func addClassName(_ className: String) {
        do {
            _ = try delegate.addClass(className)
        } catch {
        }
    }

    public func removeClassName(_ className: String) {
        do {
            _ = try delegate.removeClass(className)
        } catch {
        }
    }

    public func toggleClassName(_ className: String) {
        do {
            _ = try delegate.toggleClass(className)
        } catch {
        }
    }

    public func value() -> String {
        do {
            return try delegate.val()
        } catch {
            return ""
        }
    }

    public func setValue(_ value: String) {
        do {
            _ = try delegate.val(value)
        } catch {
        }
    }

    public func parent() -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.parent()
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func children() -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.children().array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func childAt(_ index: Int) -> KmpSoupBridgeElement? {
        if index < 0 {
            return nil
        }
        return children().elementAt(index)
    }

    public func childSize() -> Int {
        return children().size()
    }

    public func parents() -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.parents().array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func siblingElements() -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.siblingElements().array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func nextElementSiblings() -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.nextElementSiblings().array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func previousElementSiblings() -> KmpSoupBridgeElements {
        do {
            let elements = try delegate.previousElementSiblings().array()
            return KmpSoupBridgeElements(elements: elements)
        } catch {
            return KmpSoupBridgeElements.empty()
        }
    }

    public func firstElementSibling() -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.firstElementSibling() as Element?
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func lastElementSibling() -> KmpSoupBridgeElement? {
        do {
            let element = try delegate.lastElementSibling() as Element?
            return element.map { KmpSoupBridgeElement(element: $0) }
        } catch {
            return nil
        }
    }

    public func elementSiblingIndex() -> Int {
        do {
            return Int(try delegate.elementSiblingIndex())
        } catch {
            return 0
        }
    }

    public func hasTextContent() -> Bool {
        do {
            return try delegate.hasText()
        } catch {
            return false
        }
    }

    public func matchesCss(_ cssQuery: String) -> Bool {
        do {
            return try delegate.iS(cssQuery)
        } catch {
            return false
        }
    }

    public func closest(_ cssQuery: String) -> KmpSoupBridgeElement? {
        var current: Element? = delegate
        while let element = current {
            if (try? element.iS(cssQuery)) == true {
                return KmpSoupBridgeElement(element: element)
            }
            current = (try? element.parent()) ?? nil
        }
        return nil
    }

    public func setTagName(_ tagName: String) {
        do {
            _ = try delegate.tagName(tagName)
        } catch {
        }
    }

    public func setHtml(_ html: String) {
        do {
            _ = try delegate.html(html)
        } catch {
        }
    }

    public func prependHtml(_ html: String) {
        do {
            _ = try delegate.prepend(html)
        } catch {
        }
    }

    public func appendHtml(_ html: String) {
        do {
            _ = try delegate.append(html)
        } catch {
        }
    }

    public func beforeHtml(_ html: String) {
        do {
            _ = try delegate.before(html)
        } catch {
        }
    }

    public func afterHtml(_ html: String) {
        do {
            _ = try delegate.after(html)
        } catch {
        }
    }

    public func wrapHtml(_ html: String) {
        do {
            _ = try delegate.wrap(html)
        } catch {
        }
    }

    public func unwrapNode() {
        do {
            _ = try delegate.unwrap()
        } catch {
        }
    }

    public func emptyNode() {
        do {
            _ = try delegate.empty()
        } catch {
        }
    }

    public func removeNode() {
        do {
            _ = try delegate.remove()
        } catch {
        }
    }

    public func html() -> String {
        do {
            return try delegate.html()
        } catch {
            return ""
        }
    }

    public func outerHtml() -> String {
        do {
            return try delegate.outerHtml()
        } catch {
            return ""
        }
    }
}

@objcMembers
public final class KmpSoupBridgeElements: NSObject {

    private let elements: [Element]

    fileprivate init(elements: [Element]) {
        self.elements = elements
        super.init()
    }

    fileprivate static func empty() -> KmpSoupBridgeElements {
        return KmpSoupBridgeElements(elements: [])
    }

    public func size() -> Int {
        return elements.count
    }

    public func isEmpty() -> Bool {
        return elements.isEmpty
    }

    public func firstOrNull() -> KmpSoupBridgeElement? {
        return elements.first.map { KmpSoupBridgeElement(element: $0) }
    }

    public func lastOrNull() -> KmpSoupBridgeElement? {
        return elements.last.map { KmpSoupBridgeElement(element: $0) }
    }

    public func elementAt(_ index: Int) -> KmpSoupBridgeElement? {
        if index < 0 || index >= elements.count {
            return nil
        }
        return KmpSoupBridgeElement(element: elements[index])
    }

    public func text() -> String {
        var values: [String] = []
        for element in elements {
            let value = (try? element.text()) ?? ""
            if !value.isEmpty {
                values.append(value)
            }
        }
        return values.joined(separator: " ")
    }

    public func attr(_ name: String) -> String {
        guard let first = elements.first else {
            return ""
        }
        return (try? first.attr(name)) ?? ""
    }
}

@objcMembers
public final class SwiftSoupBridgeSample: NSObject {

    public func parseAndReadFirstLink(_ html: String) -> String {
        guard let document = KmpSoupBridgeParser.parseHtml(html) else {
            return ""
        }

        let title = document.title()
        let firstLink = document.select("a").firstOrNull()
        let href = firstLink?.attr("href") ?? ""

        return "title=\(title), href=\(href)"
    }
}
