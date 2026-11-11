"""BPE didático sobre caracteres Unicode (não é byte-level BPE)."""
from __future__ import annotations
from dataclasses import dataclass

@dataclass
class BPETokenizer:
    tokens: list[str]
    merges: list[tuple[str, str]]

    @classmethod
    def train(cls, text: str, vocab_size: int = 256, alphabet_text: str | None = None) -> "BPETokenizer":
        if vocab_size < 2: raise ValueError("vocab_size precisa ser pelo menos 2")
        sequence=list(text)
        # O alfabeto pode incluir caracteres da validação, mas as fusões são aprendidas só no treino.
        vocabulary=set(alphabet_text if alphabet_text is not None else text)
        merges=[]
        while len(vocabulary)<vocab_size and len(sequence)>1:
            pairs={}
            for pair in zip(sequence,sequence[1:]): pairs[pair]=pairs.get(pair,0)+1
            if not pairs: break
            pair,count=max(pairs.items(),key=lambda item:(item[1],item[0]))
            if count<2: break
            merged=pair[0]+pair[1]
            if merged in vocabulary: break
            merges.append(pair);vocabulary.add(merged)
            result=[];i=0
            while i<len(sequence):
                if i+1<len(sequence) and (sequence[i],sequence[i+1])==pair:
                    result.append(merged);i+=2
                else: result.append(sequence[i]);i+=1
            sequence=result
        return cls(tokens=["<unk>",*sorted(vocabulary)],merges=merges)

    @property
    def vocab_size(self): return len(self.tokens)

    def encode(self,text:str)->list[int]:
        sequence=list(text)
        for left,right in self.merges:
            merged=left+right;result=[];i=0
            while i<len(sequence):
                if i+1<len(sequence) and sequence[i]==left and sequence[i+1]==right:
                    result.append(merged);i+=2
                else: result.append(sequence[i]);i+=1
            sequence=result
        stoi={token:index for index,token in enumerate(self.tokens) if index}
        return [stoi.get(token,0) for token in sequence]

    def decode(self,token_ids:list[int])->str:
        return "".join(self.tokens[i] if 0<i<len(self.tokens) else "�" for i in token_ids)

    def to_dict(self)->dict:
        return {"tokens":self.tokens,"merges":[list(pair) for pair in self.merges]}

    @classmethod
    def from_dict(cls,data:dict)->"BPETokenizer":
        return cls(tokens=data["tokens"],merges=[tuple(pair) for pair in data["merges"]])
