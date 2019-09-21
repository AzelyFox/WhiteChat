function List() {
    this.listSize = 0;
    this.pos = 0;
    this.dataStore = [];
    this.clear = clear;
    this.find = find;
    this.toString = toString;
    this.insert = insert;
    this.append = append;
    this.remove = remove;
    this.front = front;
    this.end = end;
    this.prev = prev;
    this.next = next;
    this.length = length;
    this.currPos = currPos;
    this.moveTo = moveTo;
    this.getElement = getElement;
    this.length = length;
    this.contains = contains;
    this.get = get;
}
 
// 배열 뒤에 요소 추가하는 함수
function append(element) {
    this.dataStore[this.listSize++] = element;
}
 
// 요소의 index를 구하는 함수 (존재하지 않는 경우 -1 반환)
function find(element) {
    for(var i=0; i<this.dataStore.length; i++) {
        if(this.dataStore[i] == element) {
            return i;
        }
    }
    return -1;
}
 
// 요소를 삭제하고 그 결과를 boolean 으로 반환하는 함수
function remove(element){
    var foundAt = this.find(element);
 
    if(foundAt > -1) {
        this.dataStore.splice(foundAt, 1);
        --this.listSize;
        return true;
    }
    return false;
}
 
// 리스트의 길이를 반환하는 함수
function length(){
    return this.listSize;
}
 
// 리스트 요소를 확인하는 함수
function toString() {
    return "["+this.dataStore+"]";
}
 
 
// 요소를 원하는 위치에 추가하고 그 결과를 boolean 으로 반환하는 함수
function insert(element, after){
    var insertPos = this.find(after);
    if(insertPos > -1) {
        this.dataStore.splice(insertPos+1, 0, element);
        ++this.listSize;
        return true;
    }
    return false;
}
 
 
// 리스트의 모든 요소를 삭제하는 함수
function clear() {
    delete this.dataStore;
    this.dataStore.length = 0;
    this.listSize = 0;
}
 
// 리스트에 특정 요소가 있는지 그 결과를 boolean 으로 반환하는 함수
function contains(element) {
    for(var i=0;i<this.dataStore.length;i++) {
        if(this.dataStore[i] == element) {
            return true;
        }
    }
    return false;
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 맨 앞을 보게 한다.
function front() {
    this.pos = 0;
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 맨 뒤를 보게 한다.
function end() {
    this.pos = this.listSize-1;
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 이전 위치를 보게 한다.
function prev() {
    if(this.pos > 0)
        --this.pos;
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 다음 위치를 보게 한다.
function next() {
    if(this.pos < this.listSize-1) {
        ++this.pos;
    }
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 보고 있는 현재 위치를 반환 한다.
function currPos() {
    return this.pos;
}
 
// 리스트 탐색 관련 기능을 위한 pos 가 특정 위치를 보게 한다.
function moveTo(position) {
    this.pos = position;
}
 
// pos 가 보고 있는 현재 위치의 값을 반환 한다.
function getElement(){
    return this.dataStore[this.pos];
}
 
// index 로 요소를 반환하는 함수
function get(position){
    return this.dataStore[position];
}

module.exports = List;